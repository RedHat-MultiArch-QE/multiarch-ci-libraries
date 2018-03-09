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
      host.initialized = true
      
      // Install ssh keys so that either cinch or direct ssh will connect
      script.withCredentials(
        [
          script.file(credentialsId: config.sshPrivKeyCredentialId, variable: 'SSHPRIVKEY'),
          script.file(credentialsId: config.sshPubKeyCredentialId, variable: 'SSHPUBKEY'),
          [ 
            $class: 'UsernamePasswordMultiBinding', credentialsId: config.jenkinsSlaveCredentialId,
            usernameVariable: 'JENKINS_SLAVE_USERNAME', passwordVariable: 'JENKINS_SLAVE_PASSWORD'
          ]
        ]
      ) 
      {
        script.env.HOME = "/home/jenkins"
        script.sh """
          mkdir -p ~/.ssh
          cp ${script.SSHPRIVKEY} ~/.ssh/id_rsa
          cp ${script.SSHPUBKEY} ~/.ssh/id_rsa.pub
          chmod 600 ~/.ssh/id_rsa
          chmod 644 ~/.ssh/id_rsa.pub
        """

        def templateData = getTemplateData(host.arch)
        templateData.extra_vars = "{" +
          "\"rpm_key_imports\":[]," +
          "\"jenkins_master_repositories\":[]," +
          "\"jenkins_master_download_repositories\":[]," +
          "\"jslave_name\":\"${host.name}\"," +
          "\"jslave_label\":\"${host.name}\"," +
          "\"arch\":\"${host.arch}\"," +
          "\"jenkins_master_url\":\"${config.jenkinsMasterUrl}\"," +
          "\"jenkins_slave_username\":\"${script.JENKINS_SLAVE_USERNAME}\"," +
          "\"jenkins_slave_password\":\"${script.JENKINS_SLAVE_PASSWORD}\"," +
          "\"jswarm_extra_args\":\"${config.jswarmExtraArgs}\"," +
          '"jenkins_slave_repositories":[{ "name": "epel", "mirrorlist": "https://mirrors.fedoraproject.org/metalink?arch=$basearch&repo=epel-7"}]' +
          "}"
        
	script.sh "linchpin --workspace ${config.provisioningWorkspaceDir} --template-data \'${templateData}\' --verbose up ${host.target}"

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
      }

      if (config.runOnSlave) {
        host.connectedToMaster = true

        // We only care if the install ansible flag is set when we are running on the provisioned host
        // This is because if we are running on the centos container, ansible has been installed already to support linchpin & cinch
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
          }
          host.ansibleInstalled = true
        }
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
    if (!host || !host.initialized) {
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

    if (host.initialized) {
      try {
        script.sh "linchpin --workspace ${config.provisioningWorkspaceDir} --template-data \'${getTemplateData(arch)}\' --verbose destroy ${host.target}"
      } catch (e) {
        script.echo "${e}"
      }
    }

    if (host.error) {
      script.currentBuild.result = 'FAILURE'
    }
  }

  String getTemplateData(String arch) {
    // Build template data
    def templateData = [:]
    templateData.arch = arch
    templateData.job_group = config.jobgroup
    templateData.hostrequires = config.hostrequires
    //templateData.hooks = [postUp: [connectToMaster: config.runOnSlave]]

    def templateDataJson = JsonOutput.toJson(templateData)
    script.echo templateDataJson

    templateDataJson
  }
}
