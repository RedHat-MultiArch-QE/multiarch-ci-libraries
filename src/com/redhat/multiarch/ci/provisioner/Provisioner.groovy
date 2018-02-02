package com.redhat.multiarch.ci.provisioner

import groovy.json.*

class Provisioner {
  def script
  ProvisioningConfig config

  Provisioner(def script, ProvisioningConfig config) {
    this.script = script
    this.config = config
  }

  /**
   * Attempts to provision a multi-arch host.
   *
   * @param arch String representing architecture of the host to provision.
   */
  Host provision(String arch) {
    Host host = new Host(
      arch: arch,
      target: 'jenkins-slave',
      name: "${arch}-slave"
    )

    try {
      script.withCredentials([script.file(credentialsId: config.keytabCredentialId, variable: 'KEYTAB')]) {
        script.sh "kinit ${config.krbPrincipal} -k -t ${script.KEYTAB}"

        // Test to make sure we can authenticate.
        script.sh 'bkr whoami'
      }

      if (config.provisioningRepoUrl != null) {
        // Get linchpin workspace
        script.git(url: config.provisioningRepoUrl, branch: config.provisioningRepoRef)
      } else {
        script.checkout script.scm
      }

      // Attempt provisioning
      script.sh "linchpin --workspace ${config.provisioningWorkspaceDir} --template-data \'${getTemplateData(host.arch)}\' --verbose up ${host.target}"

      // We need to scan for inventory file. Please see the following for reasoning:
      // - https://github.com/CentOS-PaaS-SIG/linchpin/issues/430
      // Possible solutions to not require the scan:
      // - https://github.com/CentOS-PaaS-SIG/linchpin/issues/421
      // - overriding [evars] section and specifying inventory_file
      //
      host.inventory = script.sh (returnStdout: true, script: """
        readlink -f ${config.provisioningWorkspaceDir}/inventories/*.inventory
        """).trim()
      host.provisioned = true

      // Install ssh keys so that either cinch or direct ssh will connect
      script.withCredentials([script.file(credentialsId: config.sshPrivKeyCredentialId, variable: 'SSHPRIVKEY'),
                              script.file(credentialsId: config.sshPubKeyCredentialId, variable: 'SSHPUBKEY')])
      {
        script.env.HOME = "/home/jenkins"
        script.sh """
          mkdir -p ~/.ssh
          cp ${script.SSHPRIVKEY} ~/.ssh/id_rsa
          cp ${script.SSHPUBKEY} ~/.ssh/id_rsa.pub
          chmod 600 ~/.ssh/id_rsa
          chmod 644 ~/.ssh/id_rsa.pub
        """
      }

      if (config.runOnSlave) {
        script.withCredentials([
          [
            $class: 'UsernamePasswordMultiBinding',
            credentialsId: config.jenkinsSlaveCredentialId,
            usernameVariable: 'JENKINS_SLAVE_USERNAME',
            passwordVariable: 'JENKINS_SLAVE_PASSWORD'
          ]
        ]) {
          def extraVars = "'{" +
            "\"rpm_key_imports\":[]," +
            "\"jenkins_master_repositories\":[]," +
            "\"jenkins_master_download_repositories\":[]," +
            "\"jslave_name\":\"${host.name}\"," +
            "\"jslave_label\":\"${host.name}\"," +
            "\"arch\":\"${host.arch}\"," +
            "\"jenkins_master_url\":\"${config.jenkinsMasterUrl}\"," +
            "\"jenkins_slave_username\":\"${script.JENKINS_SLAVE_USERNAME}\"," +
            "\"jenkins_slave_password\":\"${script.JENKINS_SLAVE_PASSWORD}\"," +
            "\"jswarm_extra_args\":\"${config.jswarmExtraArgs}\"" +
            "}'"

          script.sh "cinch ${host.inventory} --extra-vars ${extraVars}"
          host.connectedToMaster = true
        }
      }

      if (config.installAnsible) {
        script.node (host.name) {
          script.sh '''
            sudo yum install python-devel openssl-devel libffi-devel -y &&
            sudo mkdir /home/jenkins &&
            sudo chown --recursive ${USER}:${USER} /home/jenkins &&
            sudo pip install --upgrade pip &&
            sudo pip install --upgrade setuptools &&
            sudo pip install --upgrade ansible
          '''
          //   echo "[defaults]" | tee -a ~/.ansible.cfg
          //   echo "remote_tmp = /tmp/${USER}/ansible" | tee -a ~/.ansible.cfg
        }
        host.ansibleInstalled = true
      }
    } catch (e) {
      script.echo "${e}"
      host.error = e.getMessage()
    }

    host
  }

  /**
   * Runs a teardown for provisioned host.
   *
   * @param host Provisioned host to be torn down.
   * @param arch String specifying the arch to run tests on.
   */
  def teardown(Host host, String arch) {
    // Prepare the cinch teardown inventory
    if (!host || !host.provisioned) {
      // The provisioning job did not successfully provision a machine, so there is nothing to teardown
      script.currentBuild.result = 'SUCCESS'
      return
    }

    // Run cinch teardown if runOnSlave was attempted with a provisioned host
    if (config.runOnSlave && host.provisioned) {
      try {
        script.sh "teardown ${host.inventory}"
      } catch (e) {
        script.echo "${e}"
      }
    }

    try {
      script.sh "linchpin --workspace ${config.provisioningWorkspaceDir} --template-data \'${getTemplateData(arch)}\' --verbose destroy ${host.target}"
    } catch (e) {
      script.echo "${e}"

      if (host.error) {
        script.currentBuild.result = 'FAILURE'
      }
    }
  }

  String getTemplateData(String arch) {
    // Build template data
    def templateData = [:]
    templateData.arch = arch
    templateData.job_group = config.jobgroup
    templateData.hostrequires = config.hostrequires

    def templateDataJson = JsonOutput.toJson(templateData)
    script.echo templateDataJson

    templateDataJson
  }
}
