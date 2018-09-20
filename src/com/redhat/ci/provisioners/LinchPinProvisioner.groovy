package com.redhat.ci.provisioners

import com.redhat.ci.Utils
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.ProvisioningException
import com.redhat.ci.provisioner.Mode
import com.redhat.ci.provisioner.Type
import groovy.json.JsonOutput

/**
 * Uses LinchPin and the libraries defined workspace to provision resources.
 */
class LinchPinProvisioner extends AbstractProvisioner {

    private static final String ACTIVATE_VIRTUALENV = '. /home/jenkins/envs/provisioner/bin/activate;'
    private static final String DEBUG_WORKSPACE = 'ls -a workspace;'
    private static final Map<String, String> LINCHPIN_TARGETS = [
        (com.redhat.ci.provider.Type.BEAKER):'beaker-slave',
    ]

    LinchPinProvisioner() {
        this(null)
    }

    LinchPinProvisioner(Script script) {
        super(script)
        if (script) {
            this.available = true
        }
        this.type = Type.LINCHPIN
        this.supportedHostTypes = [com.redhat.ci.host.Type.VM, com.redhat.ci.host.Type.BAREMETAL]
        this.supportedProviders = [com.redhat.ci.provider.Type.BEAKER]
    }

    ProvisionedHost provision(TargetHost target, ProvisioningConfig config) {
        ProvisionedHost host = new ProvisionedHost(target)
        host.displayName = "${target.arch}-slave"
        host.provisioner = this.type
        host.provider = com.redhat.ci.provider.Type.BEAKER

        try {
            // Install keys we can connect via JNLP or SSH
            Utils.installCredentials(script, config)

            // Get LinchPin workspace
            if (config.provisioningRepoUrl != null) {
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
            script.sh(
                DEBUG_WORKSPACE +
                    ACTIVATE_VIRTUALENV +
                    "linchpin -vvv --workspace ${config.provisioningWorkspaceDir} " +
                    "--template-data \'${getTemplateData(host, config)}\' " +
                    "--verbose up ${LINCHPIN_TARGETS[host.provider]}"
            )
            script.sh(DEBUG_WORKSPACE)

            // Parse the latest run info
            Map linchpinLatest = script.readJSON(file:"${config.provisioningWorkspaceDir}/resources/linchpin.latest")

            // Populate the linchpin transaction ID, inventory path, and hostname
            host.linchpinTxId = getLinchpinTxId(linchpinLatest)
            host.inventoryPath = getLinchpinInventoryPath(linchpinLatest, host)
            host.hostname = getHostname(host)
            script.echo("linchpinTxId:${host.linchpinTxId}")
            script.echo("inventoryPath:${host.inventoryPath}")
            script.echo("hostname:${host.hostname}")

            // Parse the inventory file for the name of the master node
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
            script.echo("Exception: ${e.message}")
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
                script.echo("Exception: ${e.message}")
            }
        }

        if (host.initialized) {
            try {
                script.sh(
                    DEBUG_WORKSPACE +
                        ACTIVATE_VIRTUALENV +
                        "linchpin -vvv --workspace ${config.provisioningWorkspaceDir} " +
                        "--template-data \'${getTemplateData(host, config)}\' " +
                        "--verbose destroy ${LINCHPIN_TARGETS[host.provider]} " +
                        "--tx-id ${host.linchpinTxId}"
                )
            } catch (e) {
                script.echo("Exception: ${e.message}")
            }
        }

        if (host.error) {
            script.currentBuild.result = 'FAILURE'
        }
    }

    private String getTemplateData(ProvisionedHost host, ProvisioningConfig config) {
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

    private Integer getLinchpinTxId(Map linchpinLatest) {
        // Ensure keySet size is what is expected
        int keySetSize = linchpinLatest.keySet().size()
        if (keySetSize != 1) {
            throw new ProvisioningException("LinchPin latest run keySet size is invalid. Expected 1, got ${keySetSize}")
        }

        linchpinLatest.keySet().first().toInteger()
    }

    private String getLinchpinInventoryPath(Map linchpinLatest, ProvisionedHost host) {
        Map linchpinTargets = linchpinLatest["${host.linchpinTxId}"]['targets'][0]
        String linchpinTarget = LINCHPIN_TARGETS[host.provider]
        linchpinTargets[linchpinTarget]['outputs']['inventory_path'][0]
    }

    private String getHostname(ProvisionedHost host) {
        String getMasterNode = "awk '/\\[master_node\\]/{getline; print}' ${host.inventoryPath}"
        script.sh(returnStdout:true, script:getMasterNode).trim()
    }
}
