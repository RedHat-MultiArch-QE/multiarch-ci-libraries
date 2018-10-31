package com.redhat.ci.provisioners

import com.redhat.ci.Utils
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.Mode
import com.redhat.ci.provisioner.Type

/**
 * Emulates a provisioner for a preprovisioned resource.
 */
@SuppressWarnings('AbcMetric')
class NoOpProvisioner extends AbstractProvisioner {

    private static final String PREPROVISIONED_INVENTORY = 'preprovisioned.inventory'
    private static final List<String> LAYOUT_GROUPS = [
        'all',
        'rhel7',
        'certificate_authority',
        'repositories',
        'jenkins_slave',
        'master_node',
    ]

    NoOpProvisioner(Script script) {
        super(script)
        if (script) {
            this.available = true
        }
        this.type = Type.NOOP
        this.supportedHostTypes = [
            com.redhat.ci.host.Type.BAREMETAL,
            com.redhat.ci.host.Type.CONTAINER,
            com.redhat.ci.host.Type.UNKNOWN,
            com.redhat.ci.host.Type.VM,
        ]
        this.supportedProviders = [
            com.redhat.ci.provider.Type.AWS,
            com.redhat.ci.provider.Type.BEAKER,
            com.redhat.ci.provider.Type.DUFFY,
            com.redhat.ci.provider.Type.KUBEVIRT,
            com.redhat.ci.provider.Type.OPENSHIFT,
            com.redhat.ci.provider.Type.OPENSTACK,
            com.redhat.ci.provider.Type.UNKNOWN,
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

            // A pre-provisioned host must specify the arch
            if (!host.arch) {
                host.error = 'Arch cannot be null for a pre-provisioned host.'
                return host
            }

            // A pre-provisioned host must have a hostname
            if (!host.hostname) {
                host.error = 'Hostname cannot be null for a pre-provisioned host.'
                return host
            }

            // Build out the inventory if it does not exist
            host.inventoryPath = writeInventory(host, config)
            if (!host.inventoryPath) {
                host.error = 'Inventory could not be found.'
                return host
            }

            script.echo("inventoryPath:${host.inventoryPath}")
            script.echo("hostname:${host.hostname}")
            host.provisioned = true

            if (config.mode == Mode.JNLP) {
                // Run Cinch if in JNLP mode
                script.sh(
                    ACTIVATE_VIRTUALENV +
                        "cinch ${host.inventoryPath} --extra-vars='${getCinchExtraVars(host, config)}'")
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

    private String writeInventory(ProvisionedHost host, ProvisioningConfig config) {
        String inventoryFile = null
        try {
            // Create inventory filename
            String workspaceDir = "${script.pwd()}/${PROVISIONING_DIR}/${config.provisioningWorkspaceDir}"
            String newInventoryFile = "${workspaceDir}/inventories/${PREPROVISIONED_INVENTORY}"

            // Get Cinch workspace
            if (config.mode == Mode.JNLP) {
                script.dir(PROVISIONING_DIR) {
                    if (config.provisioningRepoUrl != null) {
                        script.checkout(
                            scm:[$class:'GitSCM',
                                 userRemoteConfigs:[[url:config.provisioningRepoUrl]],
                                 branches:[[name:config.provisioningRepoRef]]],
                            poll:false)
                    } else {
                        script.checkout(script.scm)
                    }
                }
            }

            // Create inventory file, or copy it over if it was passed in
            String inventory = ''

            // Build a cinch-compatible inventory using the passing in hostname
            for (String group in LAYOUT_GROUPS) {
                inventory += "[${group}]\n${host.hostname}\n\n"
            }

            // Write and return inventory file path
            script.writeFile(file:newInventoryFile, text:inventory)
            inventoryFile = newInventoryFile
        } catch (e) {
            host.error += e.message
        }

        inventoryFile
    }
}
