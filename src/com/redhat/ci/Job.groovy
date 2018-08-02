package com.redhat.ci

import com.redhat.ci.provisioner.Provisioner
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioners.LinchPinProvisioner
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.hosts.TargetHost

/**
 * Represents a job that provisions the resources it needs, and runs @param body on them.
 */
class Job {
    Script script
    List<TargetHost> targetHosts
    ProvisioningConfig config
    Closure body
    Closure onFailure
    Closure onComplete

    /**
     * @param script      Script             WorkflowScript that the task will run in.
     * @param targetHosts List<TargetHost>   List of TargetHosts specifying what kinds of hosts the task should run on.
     * @param config      ProvisioningConfig Configuration for provisioning.
     * @param body        Closure            Closure that is run on the TargetHosts.
     * @param onFailure   Closure            Closure that is run if an Exception occurs.
     * @param onComplete  Closure            Closure that is run after all tasks are completed.
     */
    @SuppressWarnings('ParameterCount')
    Job(Script script, List<TargetHost> targetHosts, ProvisioningConfig config,
        Closure body, Closure onFailure, Closure onComplete) {
        this.script = script
        this.targetHosts = targetHosts
        this.config = config
        this.body = body
        this.onFailure = onFailure
        this.onComplete = onComplete
    }

    /**
     * Runs @body on each target host.
     * Runs @onFailure if it encounters an Exception.
     * Runs @onComplete once the taskBody is run on each targetHost.
     */
    void run() {
        Map tasks = [:]
        for (targetHost in targetHosts) {
            tasks[targetHost.id] = taskWrapper(targetHost)
        }

        // Run single host task in parallel on each arch
        script.parallel(tasks)

        // Run the onComplete closure now that the subTasks have completed
        script.node("provisioner-${config.version}") {
            onComplete()
        }
    }

    private ProvisionedHost provision(Provisioner provisioner, TargetHost targetHost) {
        ProvisionedHost host = null
        script.stage('Provision Host') {
            host = provisioner.provision(targetHost)

            // Property validity check
            if (!host || !host.hostname || !host.arch || !host.type) {
                script.error("Invalid provisioned host: ${host}")
            }

            // If the provision failed, there will be an error
            if (host.error) {
                script.error(host.error)
            }
        }
        host
    }

    private void teardown(Provisioner provisioner, ProvisionedHost host) {
        try {
            // Ensure teardown runs before the pipeline exits
            script.stage ('Teardown Host') {
                provisioner.teardown(host)
            }
        } catch (e) {
            echo("Ignoring exception in teardown: ${e}")
        }
    }

    private void runOnTarget(TargetHost targetHost) {
        // Create an instance of the provisioner
        Provisioner provisioner = new LinchPinProvisioner(script, config)

        script.node("provisioner-${config.version}") {
            ProvisionedHost host = null
            try {
                host = provision(provisioner, targetHost)
            } catch (e) {
                onFailure(e, host)
                teardown(provisioner, host)
                return
            }

            if (config.runOnSlave) {
                script.node(host.displayName) {
                    try {
                        body(host, config)
                    } catch (e) {
                        onFailure(e, host)
                    }
                }

                teardown(provisioner, host)
                return
            }

            try {
                body(host, config)
            } catch (e) {
                onFailure(e, host)
            } finally {
                teardown(provisioner, host)
            }
        }
    }

    private Closure taskWrapper(TargetHost targetHost) {
        Closure wrapTask = { target -> { -> runOnTarget(target) } }
        wrapTask(targetHost)
    }
}
