package com.redhat.ci.provisioners

import static com.redhat.ci.host.Type.UNKNOWN
import static com.redhat.ci.host.Type.VM
import static com.redhat.ci.host.Type.BAREMETAL

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

    private static final String HYPERVISOR = 'hypervisor'

    private static final Map<String, String> DEFAULT_TARGETS = [
        (com.redhat.ci.provider.Type.BEAKER):'beaker-slave',
    ]

    private static final Map<String, String> REQUIRES_VM = [
        tag:HYPERVISOR, op:'!=', value:'',
    ]

    private static final Map<String, String> REQUIRES_BAREMETAL = [
        tag:HYPERVISOR, op:'=', value:'',
    ]

    LinchPinProvisioner(Script script) {
        super(script)
        if (script) {
            this.available = true
        }
        this.type = Type.LINCHPIN
        this.supportedHostTypes = [VM, BAREMETAL]
        this.supportedProviders = [com.redhat.ci.provider.Type.BEAKER]
    }

    @SuppressWarnings(['AbcMetric', 'UnnecessaryObjectReferences'])
    ProvisionedHost provision(TargetHost target, ProvisioningConfig config) {
        ProvisionedHost host = new ProvisionedHost(target)
        try {
            // Set the default provisioning values
            host.displayName = "${target.arch}-slave"
            host.provisioner = this.type
            host.provider = com.redhat.ci.provider.Type.BEAKER
            host.typePriority = filterSupportedHostTypes(host.typePriority)
            host.type = host.typePriority.size() == 1 ? host.typePriority[0] : UNKNOWN
            host.arch = host.arch ?: 'x86_64'
            host.distro = host.distro ?: 'RHEL-ALT-7.5'
            host.variant = host.variant ?: 'Server'
            host.linchpinTarget = host.linchpinTarget ?: DEFAULT_TARGETS[host.provider]

            // Install keys we can connect via JNLP or SSH
            Utils.installCredentials(script, config)

            // Get LinchPin workspace
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
            host.initialized = true

            // Attempt provisioning
            String workspaceDir = "${PROVISIONING_DIR}/${config.provisioningWorkspaceDir}"
            try {
                script.sh(
                    ACTIVATE_VIRTUALENV +
                        "linchpin --workspace ${workspaceDir} " +
                        "--config ${workspaceDir}/linchpin.conf " +
                        "--template-data \'${getTemplateData(host, config)}\' " +
                        "--verbose up ${host.linchpinTargetEnabled ? host.linchpinTarget : ''}"
                )
            } catch (e) {
                host.error = e.message
            }

            // Parse the latest run info
            Map linchpinLatest = script.readJSON(file:"${workspaceDir}/resources/linchpin.latest")

            // Populate the linchpin transaction ID, inventory path, and hostname
            host.linchpinTxId = getLinchpinTxId(linchpinLatest)
            script.echo("linchpinTxId:${host.linchpinTxId}")

            host.inventoryPath = getLinchpinInventoryPath(linchpinLatest, host)
            script.echo("inventoryPath:${host.inventoryPath}")
            script.sh("cat ${host.inventoryPath}")

            host.hostname = getHostname(host)
            script.echo("hostname:${host.hostname}")

            // Parse the inventory file for the name of the master node
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
            }

            // Install credentials directly on the provisioned host
            if (config.installCredentials || config.installRhpkg) {
                Utils.installCredentials(script, config, host)
            }

            // We can install the RHPKG tool if the user intends to use it.
            if (config.installRhpkg) {
                Utils.installRhpkg(script, config, host)
            }
        } catch (e) {
            host.error = host.error ? host.error + ", ${e.message}" : e.message
            script.echo("Error provisioning from LinchPin: ${host.error}")
            script.echo("Stacktrace: $e.stackTrace")
        }

        // An error occured, so we should ensure resources are cleaned up
        if (host.error) {
            teardown(host, config)
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
        if (!host || !config.teardown) {
            script.echo(TEARDOWN_NOOP)
            return
        }

        // Host exists, so if there's an error, the job should fail
        if (host.error) {
            script.currentBuild.result = 'FAILURE'
        }

        // The provisioning job did not successfully provision a machine,
        // so there is nothing to teardown
        if (!host.initialized) {
            script.echo(TEARDOWN_NOOP)
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

        String workspaceDir = "${PROVISIONING_DIR}/${config.provisioningWorkspaceDir}"
        try {
            script.sh(
                ACTIVATE_VIRTUALENV +
                    "linchpin --workspace ${workspaceDir} " +
                    "--config ${workspaceDir}/linchpin.conf " +
                    "-vvvv destroy --tx-id ${host.linchpinTxId}"
            )
        } catch (e) {
            script.echo("Exception: ${e.message}")
        }
    }

    private String getTemplateData(ProvisionedHost host, ProvisioningConfig config) {
        Map templateData = [
            arch:host.arch,
            distro:host.distro,
            variant:host.variant,
            ks_meta:host.bkrKsMeta,
            kernel_options:host.bkrKernelOptions,
            kernel_options_post:host.bkrKernelOptionsPost,
            method:host.bkrMethod,
            reserve_duration:host.reserveDuration,
            job_group:host.bkrJobGroup ?: config.jobgroup,
            hostrequires:getHostRequires(host, config),
            keyvalue:host.bkrKeyValue,
            inventory_vars:host.inventoryVars,
        ]

        JsonOutput.toJson(templateData)
    }

    private List<Map> getHostRequires(ProvisionedHost host, ProvisioningConfig config) {
        List<Map> hostrequires = host.bkrHostRequires ?: (config.hostrequires ?: [])

        Closure specifiesHypervisorTag = {
            requirement ->
            requirement.tag == HYPERVISOR
        }

        // If the hypervisor tag is already specified, return default list
        if (hostrequires.findAll(specifiesHypervisorTag).size() > 0) {
            return hostrequires
        }

        // If the type priority only allows a single type, add the hypervisor
        // hostrequirement manually
        if (host.typePriority && host.typePriority.size() == 1) {
            switch (host.type) {
                case VM:
                    hostrequires.add(REQUIRES_VM)
                    break
                case BAREMETAL:
                    hostrequires.add(REQUIRES_BAREMETAL)
                    break
                default:
                    // Do nothing
                    break
            }
        }

        hostrequires
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
        linchpinTargets[host.linchpinTarget]['outputs']['inventory_path'][0]
    }

    private String getHostname(ProvisionedHost host) {
        String getMasterNode = "awk '/\\[master_node\\]/{getline; print}' ${host.inventoryPath} | cut -d ' ' -f 1"
        script.sh(returnStdout:true, script:getMasterNode).trim()
    }
}
