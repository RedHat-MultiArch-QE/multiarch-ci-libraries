package com.redhat.ci

import java.util.logging.Logger
import java.util.logging.Level
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.ProvisioningService
import com.redhat.ci.provisioner.ProvisioningException
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.hosts.TargetHost

/**
 * Represents a job that provisions the resources it needs, and runs @param body on them.
 */
class Job {
    private static final Logger LOG = Logger.getLogger(Job.name)
    protected static final String SANDBOX_DIR = 'sandbox'

    protected Script script
    protected List<TargetHost> targetHosts
    protected ProvisioningConfig config
    protected Closure body
    protected Closure onFailure
    protected Closure onComplete
    protected ProvisioningService provSvc

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
            subJobs[targetHost.name ?: targetHost.id] = jobWrapper(targetHost)
        }

        // Run each single host job in parallel on each specified host
        script.parallel(subJobs)

        // Run the onComplete closure now that the subJobs have completed
        script.node("provisioner-${config.version}") {
            runInDirectory(SANDBOX_DIR) {
                onComplete()
            }
        }
    }

    protected ProvisionedHost provision(TargetHost targetHost) {
        ProvisionedHost host = null
        host = provSvc.provision(targetHost, config, script)

        // Null check
        if (!host) {
            script.echo("Invalid provisioned host: ${host}")
            return host
        }

        // If the provision failed, there will be an error
        if (host.error) {
            throw new ProvisioningException(host.error)
        }

        // Property validity check
        if (!host.hostname || !host.arch || !host.type ||
            !host.provisioner || !host.provider) {
            throw new ProvisioningException('Invalid provisioned host: [' +
                                            "hostname:${host.hostname}, " +
                                            "arch:${host.arch}, " +
                                            "type:${host.type}, " +
                                            "provisioner:${host.provisioner}, " +
                                            "provider:${host.provider}" +
                                            ']')
        }

        host
    }

    protected void teardown(ProvisionedHost host) {
        if (!host || !host.provisioner) {
            // If there isn't a host or a set provisioner, skip teardown
            script.echo('Skipping teardown since host is null or host.provisioner is not set')
            return
        }

        try {
            // Ensure teardown runs before the pipeline exits
            provSvc.teardown(host, config, script)
        } catch (e) {
            script.echo("Ignoring exception in teardown: ${e}")
        }
    }

    protected void runOnTarget(TargetHost targetHost) {
        script.node("provisioner-${config.version}") {
            ProvisionedHost host = new ProvisionedHost(targetHost)
            try {
                host = provision(targetHost)
            } catch (e) {
                LOG.log(Level.SEVERE, "Exception: ${e.message}", e)
                script.echo("Exception: ${e.message}")
                runInDirectory(SANDBOX_DIR) {
                    onFailure(e, host)
                }
                teardown(host)
                return
            }

            if (config.runOnSlave) {
                script.node(host.displayName) {
                    try {
                        runInDirectory(SANDBOX_DIR) {
                            body(host, config)
                        }
                    } catch (e) {
                        script.echo("Exception: ${e.message}")
                        runInDirectory(SANDBOX_DIR) {
                            onFailure(e, host)
                        }
                    }
                }

                teardown(host)
                return
            }

            try {
                runInDirectory(SANDBOX_DIR) {
                    body(host, config)
                }
            } catch (e) {
                script.echo("Exception: ${e.message}")
                runInDirectory(SANDBOX_DIR) {
                    onFailure(e, host)
                }
            } finally {
                teardown(host)
            }
        }
    }

    private Closure jobWrapper(TargetHost targetHost) {
        Closure wrapJob = { target -> { -> runOnTarget(target) } }
        wrapJob(targetHost)
    }

    protected void runInDirectory(String dir, Closure body) {
        script.dir(dir) {
            body()
        }
    }
}
