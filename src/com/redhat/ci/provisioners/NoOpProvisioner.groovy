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
 * Emulates a provisioner for a preprovisioned resource.
 */
class NoOPProvisioner extends AbstractProvisioner {

    private static final String PREPROVISIONED_INVENTORY = 'preprovisioned.inventory'

    NoOPProvisioner(Script script) {
        super(script)
        if (script) {
            this.available = true
        }
        this.type = Type.NOOP
        this.supportedHostTypes = [
            com.redhat.ci.host.CONTAINER, 
            com.redhat.ci.host.Type.VM,
            com.redhat.ci.host.Type.BAREMETAL
        ]
        this.supportedProviders = [
            null
        ]
    }

    ProvisionedHost provision(TargetHost target, ProvisioningConfig config) {
        ProvisionedHost host = new ProvisionedHost(target)
        host.displayName = "${target.arch}-slave"
        host.provisioner = this.type

        try {
            // Install keys we can connect via JNLP or SSH
            Utils.installCredentials(script, config)
            host.initialized = true

            // Attempt provisioning
            String workspaceDir = "${PROVISIONING_DIR}/${config.provisioningWorkspaceDir}"

            // Build out the inventory if it does not exist
            if (!host.inventoryPath) {
                createInventory(host)
                host.inventoryPath = PREPROVISIONED_INVENTORY
            }

            // A pre-provisioned host must have a hostname
            if (!host.hostname) {
                error += 'Hostname cannot be null for a pre-provisioned host.'
                return host
            }

            script.echo("inventoryPath:${host.inventoryPath}")
            script.echo("hostname:${host.hostname}")

            // Parse the inventory file for the name of the master node
            host.provisioned = true

            if (config.mode == Mode.JNLP) {
                // Run Cinch if in JNLP mode
                script.sh(
                    ACTIVATE_VIRTUALENV +
                        "cinch ${host.inventoryPath} --extra-vars='${getExtraVars(host, config)}'")
                host.connectedToMaster = true

                // In JNLP mode, we can install Ansible so the user can run playbooks
                // (Already installed in SSH mode)
                if (config.installAnsible) {
                    Utils.installAnsible(script, config, host)
                }

                // In JNLP mode, install provisioning credentials directly on the provisioned host
                // (Already installed in SSH mode)
                if (config.installCredentials) {
                    Utils.installCredentials(script, config, host)
                }
            }

            // We can install the RHPKG tool if the user intends to use it.
            if (config.installRhpkg) {
                Utils.installRhpkg(script, config, host)
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
        // Check if the host was even created
        if (!host) {
            return
        }

        // Host exists, so if there's an error, the job should fail
        if (host.error) {
            script.currentBuild.result = 'FAILURE'
        }

        // The provisioning job did not successfully provision a machine,
        // so there is nothing to teardown
        if (!host.initialized) {
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
    }

    private String getTemplateData(ProvisionedHost host, ProvisioningConfig config) {
        Map templateData = [:]
        templateData.arch = host.arch
        templateData.job_group = config.jobgroup
        templateData.hostrequires = config.hostrequires

        JsonOutput.toJson(templateData)
    }

    private String getExtraVars(ProvisionedHost host, ProvisioningConfig config) {
        script.withCredentials([
            script.usernamePassword(credentialsId:config.jenkinsSlaveCredentialId,
                                    usernameVariable:'JENKINS_SLAVE_USERNAME',
                                    passwordVariable:'JENKINS_SLAVE_PASSWORD'),
        ]) {
            Map extraVars = [
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

            JsonOutput.toJson(extraVars)
        }
    }
}
