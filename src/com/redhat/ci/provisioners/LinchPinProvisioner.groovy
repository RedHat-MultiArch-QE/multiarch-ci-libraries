package com.redhat.ci.provisioners

import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost

class LinchPinProvisioner extends AbstractProvisioner {
  ProvisionedHost provision(TargetHost target) {
    ProvisionedHost host = new ProvisionedHost(
      arch: target.arch,
      linchpinTarget: target.linchpinTarget,
      displayName: "${target.arch}-slave"
    )

    try {
      installCredentials()

      if (config.provisioningRepoUrl != null) {
        // Get linchpin workspace
        script.git(url: config.provisioningRepoUrl, branch: config.provisioningRepoRef)
      } else {
        script.checkout script.scm
      }

      // Attempt provisioning
      host.initialized = true

      // Install keys we can connect via JNLP or SSH
      script.sh """
        . /home/jenkins/envs/provisioner/bin/activate
        linchpin --workspace ${config.provisioningWorkspaceDir} --template-data \'${getTemplateData(host)}\' --verbose up ${host.linchpinTarget}
      """


      // We need to scan for inventory file. Please see the following for reasoning:
      // - https://github.com/CentOS-PaaS-SIG/linchpin/issues/430
      // Possible solutions to not require the scan:
      // - https://github.com/CentOS-PaaS-SIG/linchpin/issues/421
      // - overriding [evars] section and specifying inventory_file
      //
      host.inventoryPath = script.sh(returnStdout: true, script: """
          readlink -f ${config.provisioningWorkspaceDir}/inventories/*.inventory
          """).trim()

      // Now that we have the inventory file, we should populate the hostName
      // With the name of the master node
      host.hostName = script.sh(returnStdout: true, script: """
          awk '/\\[master_node\\]/{getline; print}' ${host.inventoryPath}
          """).trim()

      host.provisioned = true

      if (config.mode == Mode.JNLP) {
        // This is JNLP mode, and we haven't encountered an Exception, so we must be connected
        host.connectedToMaster = true

        // In JNLP mode, we can install Ansible so the user can run playbooks
        // (Already installed in SSH mode)
        if (config.installAnsible) {
          installAnsible(host)
        }

        // In JNLP mode, we can install provisionining credentials directly on the provisioned host
        // (Already installed in SSH mode)
        if (config.installCredentials) {
          installCredentials(host)
        }
      }

      // We can install the RHPKG tool if the user intends to use it.
      if (config.installRhpkg) {
        installRhpkg(host)
      }
    } catch (e) {
      script.echo "${e}"
      host.error = e.getMessage()
    }

    return host
  }

  /**
   * Runs a teardown for provisioned host.
   *
   * @param host Provisioned host to be torn down.
   */
  void teardown(ProvisionedHost host) {
    // Check if the host was provisoned
    if (!host || !host.initialized) {
      // The provisioning job did not successfully provision a machine, so there is nothing to teardown
      return
    }

    // Run Cinch teardown if we're in JNLP mode and the host was connected to the master node
    if (config.mode == Mode.JNLP && host.connectedToMaster) {
      try {
        script.sh """
          . /home/jenkins/envs/provisioner/bin/activate
          teardown ${host.inventoryPath}
        """
      } catch (e) {
        script.echo "${e}"
      }
    }

    if (host.initialized) {
      try {
        script.sh """
          . /home/jenkins/envs/provisioner/bin/activate
          linchpin --workspace ${config.provisioningWorkspaceDir} --template-data \'${getTemplateData(host)}\' --verbose destroy ${host.linchpinTarget}
        """
      } catch (e) {
        script.echo "${e}"
      }
    }

    if (host.error) {
      script.currentBuild.result = 'FAILURE'
    }
  }

  String getTemplateData(ProvisionedHost host) {
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
      templateData.hooks = [postUp: [connectToMaster: config.mode == Mode.JNLP]]
      templateData.extra_vars = "{" +
        "\"rpm_key_imports\":[]," +
        "\"jenkins_master_repositories\":[]," +
        "\"jenkins_master_download_repositories\":[]," +
        "\"jslave_name\":\"${host.displayName}\"," +
        "\"jslave_label\":\"${host.displayName}\"," +
        "\"arch\":\"${host.arch}\"," +
        "\"jenkins_master_url\":\"${config.jenkinsMasterUrl}\"," +
        "\"jenkins_slave_username\":\"${script.JENKINS_SLAVE_USERNAME}\"," +
        "\"jenkins_slave_password\":\"${script.JENKINS_SLAVE_PASSWORD}\"," +
        "\"jswarm_version\":\"3.9\"," +
        "\"jswarm_filename\":\"swarm-client-{{ jswarm_version }}.jar\"," +
        "\"jswarm_extra_args\":\"${config.jswarmExtraArgs}\"," +
        '"jenkins_slave_repositories":[{ "name": "epel", "mirrorlist": "https://mirrors.fedoraproject.org/metalink?arch=$basearch&repo=epel-7"}]' +
        "}"

      def templateDataJson = JsonOutput.toJson(templateData)
      templateDataJson
    }
  }
}
