package com.redhat.ci.provisioners

import groovy.json.*
import com.redhat.ci.provisioner.Provisioner
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost

abstract class AbstractProvisioner implements Provisioner {
  def script
  ProvisioningConfig config

  AbstractProvisioner(def script, ProvisioningConfig config) {
    this.script = script
    this.config = config
  }

  /**
   * Attempts to provision the TargetHost.
   *
   * @param target TargetHost Specifies parameters for the host to provision.
   */
  abstract ProvisionedHost provision(TargetHost target)

  /**
   * Attempts a teardown of the ProvisionedHost.
   *
   * @param host ProvisionedHost Host to be torn down.
   */
  abstract void teardown(ProvisionedHost host)

  /**
   * Attemps to install Ansible.
   */
  void installAnsible(ProvisionedHost host = null) {
    installWrapper(
      host,
      { h ->
        script.sh '''
          sudo yum install python-devel openssl-devel libffi-devel -y &&
          sudo mkdir -p /home/jenkins &&
          sudo chown --recursive ${USER}:${USER} /home/jenkins &&
          sudo pip install --upgrade pip &&
          sudo pip install --upgrade setuptools &&
          sudo pip install --upgrade ansible
        '''
        if (h == null) return
        h.installedAnsible = true
      }
    )
  }

  /**
   * Attempts to install SSH and Beaker credentials.
   */
  void installCredentials(ProvisionedHost host = null) {
    installWrapper(
      host,
      { h ->
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
          if (h == null) return
          h.credentialsInstalled = true
        }
      }
    )
  }

  /**
   * Attempts to install and configure the RHPKG tool.
   */
  void installRhpkg(ProvisionedHost host = null) {
    installWrapper(
      host,
      { h ->
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
        if (h == null) return
        h.rhpkgInstalled = true
      }
    )
  }

  /**
   * A utility function that determines the host where the installation will be attempted.
   * If a provisioned host with a non-null displayName is passed in, the install step will be
   * attempted on that host; otherwise, the install with target the current node.
   */
  void installWrapper(ProvisionedHost host, Closure install) {
    // Installation should occur on current node
    if (host == null || host.displayName == null) {
      install(host)
      return
    }

    // Installation should occur on target host
    script.node(host.displayName) {
      install(host)
    }
  }
}
