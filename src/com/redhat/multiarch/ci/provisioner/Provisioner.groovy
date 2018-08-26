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
      installCredentials(script)

      if (config.provisioningRepoUrl != null) {
        // Get linchpin workspace
        script.checkout(scm:[$class: 'GitSCM', userRemoteConfigs: [[url: config.provisioningRepoUrl]], branches: [[name: config.provisioningRepoRef]]], poll:false)
      } else {
        script.checkout script.scm
      }

      // Attempt provisioning
      host.initialized = true

      // Install ssh keys so that either cinch or direct ssh will connect
      script.sh """
        . /home/jenkins/envs/provisioner/bin/activate
        linchpin --workspace ${config.provisioningWorkspaceDir} --template-data \'${getTemplateData(host)}\' --verbose up ${host.target}
      """

      // We need to scan for inventory file. Please see the following for reasoning:
      // - https://github.com/CentOS-PaaS-SIG/linchpin/issues/430
      // Possible solutions to not require the scan:
      // - https://github.com/CentOS-PaaS-SIG/linchpin/issues/421
      // - overriding [evars] section and specifying inventory_file
      //
      host.inventory = script.sh(returnStdout: true, script: """
          readlink -f ${config.provisioningWorkspaceDir}/inventories/*.inventory
          """).trim()

      // Now that we have the inventory file, we should populate the hostName
      // With the name of the master node
      host.hostName = script.sh(returnStdout: true, script: """
          awk '/\\[master_node\\]/{getline; print}' ${host.inventory}
          """).trim()

      // Let's examine this inventory file
      script.sh("cat ${host.inventory}")
      script.sh("""
        . /home/jenkins/envs/provisioner/bin/activate
        cinch ${host.inventory} --extra-vars=${getExtraVars(host)}
      """)

      host.provisioned = true

      if (config.runOnSlave) {
        host.connectedToMaster = true

        // We only care if the install ansible flag is set when we are running on the provisioned host
        // It's already installed on the provisioning container
        if (config.installAnsible) {
          script.node (host.name) {
            script.sh '''
              sudo yum install python-devel openssl-devel libffi-devel -y &&
              sudo mkdir -p /home/jenkins &&
              sudo chown --recursive ${USER}:${USER} /home/jenkins &&
              sudo pip install --upgrade pip &&
              sudo pip install --upgrade setuptools &&
              sudo pip install --upgrade ansible
            '''
          }
          host.ansibleInstalled = true
        }

        // We only care if the install credentials flag is set when we are running on the provisioned host
        // It's already installed on the provisioning container
        if (config.installCredentials) {
          script.node (host.name) {
            installCredentials(script)
          }
        }
        host.credentialsInstalled = true
      }

      if (config.installRhpkg) {
        script.node(host.name) {
          installRhpkg(script)
        }
      }
      host.rhpkgInstalled = true
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
        script.sh """
          . /home/jenkins/envs/provisioner/bin/activate
          teardown ${host.inventory}
        """
      } catch (e) {
        script.echo "${e}"
      }
    }

    if (host.initialized) {
      try {
        script.sh """
          . /home/jenkins/envs/provisioner/bin/activate
          linchpin --workspace ${config.provisioningWorkspaceDir} --template-data \'${getTemplateData(host)}\' --verbose destroy ${host.target}
        """
      } catch (e) {
        script.echo "${e}"
      }
    }

    if (host.error) {
      script.currentBuild.result = 'FAILURE'
    }
  }

  String getTemplateData(Host host) {
    script.withCredentials([
      script.usernamePassword(credentialsId: config.jenkinsSlaveCredentialId,
                              usernameVariable: 'JENKINS_SLAVE_USERNAME',
                              passwordVariable: 'JENKINS_SLAVE_PASSWORD')
    ]) {
      // Build template data
      def templateData = [:]
      templateData.arch = host.arch
      templateData.job_group = config.jobgroup
      templateData.hostrequires = config.hostrequires
      templateData.hooks = [postUp: [connectToMaster: config.runOnSlave]]
      templateData.extra_vars = '{' +
        '"rpm_key_imports":[],' +
        '"jenkins_master_repositories":[],' +
        '"jenkins_master_download_repositories":[],' +
        '"jslave_name":"' + "${host.name}"                                + '",' +
        '"jslave_label":"' + "${host.name}"                               + '",' +
        '"arch":"' + "${host.arch}"                                       + '",' +
        '"jenkins_master_url":"' + "${config.jenkinsMasterUrl}"           + '",' +
        '"jenkins_slave_username":"' + "${script.JENKINS_SLAVE_USERNAME}" + '",' +
        '"jenkins_slave_password":"' + "${script.JENKINS_SLAVE_PASSWORD}" + '",' +
        '"jswarm_version":"3.9",' +
        '"jswarm_filename":"swarm-client-{{ jswarm_version }}.jar",' +
        '"jswarm_extra_args":"' + "${config.jswarmExtraArgs}" + '",' +
        '"jenkins_slave_repositories":[{"name":"epel","mirrorlist":"https://mirrors.fedoraproject.org/metalink?arch=$basearch&repo=epel-7"}]' +
        '}'

      def templateDataJson = JsonOutput.toJson(templateData)
      templateDataJson
    }
  }


  String getExtraVars(Host host) {
    script.withCredentials([
      script.usernamePassword(credentialsId: config.jenkinsSlaveCredentialId,
                              usernameVariable: 'JENKINS_SLAVE_USERNAME',
                              passwordVariable: 'JENKINS_SLAVE_PASSWORD')
    ]) {
      // Build template data
      def extraVars = [
        "rpm_key_imports":[],
        "jenkins_master_repositories":[],
        "jenkins_master_download_repositories":[],
        "jslave_name":"${host.name}",
        "jslave_label":"${host.name}",
        "arch":"${host.arch}",
        "jenkins_master_url":"${config.jenkinsMasterUrl}",
        "jenkins_slave_username":"${script.JENKINS_SLAVE_USERNAME}",
        "jenkins_slave_password":"${script.JENKINS_SLAVE_PASSWORD}",
        "jswarm_version":"3.9",
        "jswarm_filename":"swarm-client-{{ jswarm_version }}.jar",
        "jswarm_extra_args":"${config.jswarmExtraArgs}",
      ] 
      def extraVarsJson = JsonOutput.toJson(extraVars)
      extraVarsJson
    }
  }

  void installCredentials(def script) {
    script.withCredentials([
      script.file(credentialsId: config.keytabCredentialId, variable: 'KEYTAB'),
      script.usernamePassword(credentialsId: config.krbPrincipalCredentialId,
                              usernameVariable: 'KRB_PRINCIPAL',
                              passwordVariable: ''),
      script.file(credentialsId: config.sshPrivKeyCredentialId, variable: 'SSHPRIVKEY'),
      script.file(credentialsId: config.sshPubKeyCredentialId, variable: 'SSHPUBKEY'),
      script.file(credentialsId: config.krbConfCredentialId, variable: 'KRBCONF'),
      script.file(credentialsId: config.bkrConfCredentialId, variable: 'BKRCONF')
    ]) {
      script.env.HOME = "/home/jenkins"
      script.sh """
        sudo yum install -y krb5-workstation || yum install -y krb5-workstation
        sudo cp ${script.KRBCONF} /etc/krb5.conf || cp ${script.KRBCONF} /etc/krb5.conf
        sudo mkdir -p /etc/beaker || mkdir -p /etc/beaker
        sudo cp ${script.BKRCONF} /etc/beaker/client.conf || cp ${script.BKRCONF} /etc/beaker/client.conf
        sudo chmod 644 /etc/krb5.conf || chmod 644 /etc/krb5.conf
        sudo chmod 644 /etc/beaker/client.conf || chmod 644 /etc/beaker/client.conf
        kinit ${script.KRB_PRINCIPAL} -k -t ${script.KEYTAB}
        mkdir -p ~/.ssh
        cp ${script.SSHPRIVKEY} ~/.ssh/id_rsa
        cp ${script.SSHPUBKEY} ~/.ssh/id_rsa.pub
        chmod 600 ~/.ssh/id_rsa
        chmod 644 ~/.ssh/id_rsa.pub
        eval "\$(ssh-agent -s)"
        ssh-add ~/.ssh/id_rsa
      """
    }
  }

  void installRhpkg(def script) {
    script.sh """
      echo "pkgs.devel.redhat.com,10.19.208.80 ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAplqWKs26qsoaTxvWn3DFcdbiBxqRLhFngGiMYhbudnAj4li9/VwAJqLm1M6YfjOoJrj9dlmuXhNzkSzvyoQODaRgsjCG5FaRjuN8CSM/y+glgCYsWX1HFZSnAasLDuW0ifNLPR2RBkmWx61QKq+TxFDjASBbBywtupJcCsA5ktkjLILS+1eWndPJeSUJiOtzhoN8KIigkYveHSetnxauxv1abqwQTk5PmxRgRt20kZEFSRqZOJUlcl85sZYzNC/G7mneptJtHlcNrPgImuOdus5CW+7W49Z/1xqqWI/iRjwipgEMGusPMlSzdxDX4JzIx6R53pDpAwSAQVGDz4F9eQ==" | sudo tee -a /etc/ssh/ssh_known_hosts

      echo "Host pkgs.devel.redhat.com" | sudo tee -a /etc/ssh/ssh_config
      echo "IdentityFile /home/jenkins/.ssh/id_rsa" | sudo tee -a /etc/ssh/ssh_config

      sudo yum install -y yum-utils git
      curl -L -O http://download.devel.redhat.com/rel-eng/internal/rcm-tools-rhel-7-server.repo
      sudo yum-config-manager --add-repo rcm-tools-rhel-7-server.repo
      sudo yum install -y rhpkg
      git config --global user.name "jenkins"
    """
  }
}
