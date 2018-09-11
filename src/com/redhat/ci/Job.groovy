package com.redhat.ci

import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.ProvisioningService
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
    ProvisioningService provSvc

    /**
     * @param script      Script             WorkflowScript that the job will run in.
     * @param targetHosts List<TargetHost>   List of TargetHosts specifying what kinds of hosts the job should run on.
     * @param config      ProvisioningConfig Configuration for provisioning.
     * @param body        Closure            Closure that is run on the TargetHosts.
     * @param onFailure   Closure            Closure that is run if an Exception occurs.
     * @param onComplete  Closure            Closure that is run after all jobs are completed.
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
        this.provSvc = new ProvisioningService()
    }

    /**
     * Runs @body on each target host.
     * Runs @onFailure if it encounters an Exception.
     * Runs @onComplete once the jobBody is run on each targetHost.
     */
    void run() {
        Map subJobs = [:]
        for (targetHost in targetHosts) {
            subJobs[targetHost.arch] = jobWrapper(targetHost)
        }

        // Run each single host job in parallel on each specified host
        script.parallel(subJobs)

        // Run the onComplete closure now that the subJobs have completed
        script.node("provisioner-${config.version}") {
            onComplete()
        }
    }

    private ProvisionedHost provision(TargetHost targetHost) {
        ProvisionedHost host = null
        host = provSvc.provision(targetHost, config, script)

        // Property validity check
        if (!host || !host.hostname || !host.arch || !host.type ||
            !host.provisioner || !host.provider) {
            script.echo("Invalid provisioned host: ${host.dump()}")
        }

        // If the provision failed, there will be an error
        if (host.error) {
            script.echo(host.error)
        }
        host
    }

    private void teardown(ProvisionedHost host) {
        try {
            // Ensure teardown runs before the pipeline exits
            provSvc.teardown(host, config, script)
        } catch (e) {
            script.echo("Ignoring exception in teardown: ${e}")
        }
    }

    private void runOnTarget(TargetHost targetHost) {
        script.node("provisioner-${config.version}") {
            ProvisionedHost host = new ProvisionedHost(targetHost)
            try {
                host = provision(targetHost)
            } catch (e) {
                onFailure(e, host)
                teardown(host)
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

                teardown(host)
                return
            }

            try {
                body(host, config)
            } catch (e) {
                onFailure(e, host)
            } finally {
                teardown(host)
            }
        }
    }

    private Closure jobWrapper(TargetHost targetHost) {
        Closure wrapJob = { target -> { -> runOnTarget(target) } }
        wrapJob(targetHost)
    }
}
