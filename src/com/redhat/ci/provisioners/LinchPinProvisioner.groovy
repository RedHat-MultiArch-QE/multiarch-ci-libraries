package com.redhat.ci.provisioners

import com.redhat.ci.Utils
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.Mode
import com.redhat.ci.provisioner.Type
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Uses LinchPin and the libraries defined workspace to provision resources.
 */
class LinchPinProvisioner extends AbstractProvisioner {
    private static final String ACTIVATE_VIRTUALENV = '. /home/jenkins/envs/provisioner/bin/activate\n'
    private static final Map<String, String> LINCHPIN_TARGETS = [
        (com.redhat.ci.provider.Type.BEAKER):'beaker-slave',
    ]

    LinchPinProvisioner(Script script) {
        super(script)
        this.available = true
        this.type = Type.LINCHPIN
        this.supportedHostTypes = [com.redhat.ci.host.Type.VM, com.redhat.ci.host.Type.BAREMETAL]
        this.supportedProviders = [com.redhat.ci.provider.Type.BEAKER]
    }

    @SuppressWarnings('AbcMetric')
    ProvisionedHost provision(TargetHost target, ProvisioningConfig config) {
        ProvisionedHost host = new ProvisionedHost(target)
        host.displayName = "${target.arch}-slave"
        host.provisioner = this.type
        host.provider = com.redhat.ci.provider.Type.BEAKER

        try {
            Utils.installCredentials(script, config)

            if (config.provisioningRepoUrl != null) {
                // Get LinchPin workspace
                script.checkout(
                    scm:[$class:'GitSCM',
                         userRemoteConfigs:[[url:config.provisioningRepoUrl]],
                         branches:[[name:config.provisioningRepoRef]]],
                    poll:false)
            } else {
                script.checkout(script.scm)
            }

            // Attempt provisioning
            host.initialized = true

            // Install keys we can connect via JNLP or SSH
            script.sh(
                ACTIVATE_VIRTUALENV +
                "linchpin -vvv --workspace ${config.provisioningWorkspaceDir} " +
                "--template-data \'${getTemplateData(host, config)}\' " +
                "--verbose up ${LINCHPIN_TARGETS[host.provider]}"
            )

            // Retrieve the latest linchpin transaction output
            JsonSlurper slurper = new JsonSlurper()
            String results = script.readFile("${config.provisioningWorkspaceDir}/resources/linchpin.latest")
            Map linchpinLatest = slurper.parse(results)
            if (linchpinLatest.keySet().size() != 1) {
                return host
            }

            // Parse the linchpin transaction ID
            host.linchpinTxId = linchpinLatest.keySet().toArray()[0]
            script.echo("linchpinTxId:${host.linchpinTxId}")
            Map linchpinTargets = linchpinLatest[host.linchpinTxId]['targets'][0]
            script.echo("linchpinTargets:${linchpinTargets}")
            String linchpinTarget = LINCHPIN_TARGETS[host.provider]
            script.echo("linchpinTarget:${linchpinTarget}")
            host.inventoryPath = linchpinTargets[linchpinTarget]['outputs']['inventory_path'][0]
            script.echo("inventoryPath:${host.inventoryPath}")

            // Parse the inventory file for the name of the master node
            String getMasterNode = "awk '/\\[master_node\\]/{getline; print}' ${host.inventoryPath}"
            host.hostname = script.sh(returnStdout:true, script:getMasterNode).trim()
            host.provisioned = true

            if (config.mode == Mode.JNLP) {
                // JNLP mode without Exception, so we must be connected
                host.connectedToMaster = true

                // In JNLP mode, we can install Ansible so the user can run playbooks
                // (Already installed in SSH mode)
                if (config.installAnsible) {
                    Utils.installAnsible(script, host)
                }

                // In JNLP mode, install provisioning credentials directly on the provisioned host
                // (Already installed in SSH mode)
                if (config.installCredentials) {
                    Utils.installCredentials(script, config, host)
                }
            }

            // We can install the RHPKG tool if the user intends to use it.
            if (config.installRhpkg) {
                Utils.installRhpkg(script, host)
            }
        } catch (e) {
            script.echo(e.message)
            host.error = e.message
        }

        host
    }

    /**
     * Runs a teardown for provisioned host.
     *
     * @param host Provisioned host to be torn down.
     */
    void teardown(ProvisionedHost host, ProvisioningConfig config) {
        // Check if the host was provisoned
        if (!host || !host.initialized) {
            // The provisioning job did not successfully provision a machine,
            // so there is nothing to teardown
            return
        }

        // Run Cinch teardown if we're in JNLP mode and the host was connected to the master node
        if (config.mode == Mode.JNLP && host.connectedToMaster) {
            try {
                script.sh(
                    ACTIVATE_VIRTUALENV +
                        "teardown ${host.inventoryPath}"
                )
            } catch (e) {
                script.echo(e.message)
            }
        }

        if (host.initialized) {
            try {
                script.sh(
                    ACTIVATE_VIRTUALENV +
                    "linchpin --workspace ${config.provisioningWorkspaceDir} " +
                    "--txid ${host.linchpinTxId}" +
                    "--verbose destroy ${LINCHPIN_TARGETS[host.provider]}"
                )
            } catch (e) {
                script.echo(e.message)
            }
        }

        if (host.error) {
            script.currentBuild.result = 'FAILURE'
        }
    }

    String getTemplateData(ProvisionedHost host, ProvisioningConfig config) {
        script.withCredentials(
            [
                script.usernamePassword(
                    credentialsId:config.jenkinsSlaveCredentialId,
                    usernameVariable:'JENKINS_SLAVE_USERNAME',
                    passwordVariable:'JENKINS_SLAVE_PASSWORD'
                ),
            ]
        ) {
            // Build template data
            Map templateData = [:]
            templateData.arch = host.arch
            templateData.job_group = config.jobgroup
            templateData.hostrequires = config.hostrequires
            templateData.hooks = [postUp:[connectToMaster:config.mode == Mode.JNLP]]
            templateData.extra_vars = [
                'rpm_key_imports':[],
                'jenkins_master_repositories':[],
                'jenkins_master_download_repositories':[],
                'jslave_name':"${host.displayName}",
                'jslave_label':"${host.displayName}",
                'arch':"${host.arch}",
                'jenkins_master_url':"${config.jenkinsMasterUrl}",
                'jenkins_slave_username':"${script.JENKINS_SLAVE_USERNAME}",
                'jenkins_slave_password':"${script.JENKINS_SLAVE_PASSWORD}",
                'jswarm_version':'3.9',
                'jswarm_filename':'swarm-client-{{ jswarm_version }}.jar',
                'jswarm_extra_args':"${config.jswarmExtraArgs}",
                'jenkins_slave_repositories':[[
                    'name':'epel',
                    'mirrorlist':'https://mirrors.fedoraproject.org/metalink?arch=\$basearch&repo=epel-7'
                ]]
            ]

            String templateDataJson = JsonOutput.toJson(templateData)
            templateDataJson
        }
    }
}
