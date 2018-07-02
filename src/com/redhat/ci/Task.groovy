package com.redhat.ci

import com.redhat.ci.provisioner.Provisioner
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.host.ProvisionedHost
import com.redhat.ci.host.TargetHost

class Task {
  def script
  List<TargetHost> targetHosts
  ProvisioningConfig config
  Closure taskBody
  Closure onFailure
  Closure onCompelete 

  /**
   * @param script      WorkflowScript that the task will run in.
   * @param targetHosts List<TargetHost> List of targetHosts specifying what kinds of hosts the task should run on.
   * @param config      ProvisioningConfig Configuration for provisioning.
   * @param taskBody    Closure that takes the Slave used by the task.
   * @param onFailure   Closure that take the Slave used by the task and the Exception that occured.
   * @param onComplete  Closure that takes the ProvisionedHost used by the task.
   */
  Task(def script, List<TargetHost> targetHosts, ProvisioningConfig config,
       Closure taskBody, Closure onFailure, Closure onComplete) {
    this.script = script
    this.targetHosts = targetHosts
    this.config = config
    this.taskBody = taskBody
    this.onFailure = onFailure
    this.onComplete = onComplete
  }

  /**
   * Runs @taskBody on each target host.
   * Runs @onFailure if it encounters an Exception.
   * Runs @onComplete once the taskBody is run on each targetHost.
   */
  def run() {
    def subTasks = [:]
    for (targetHost in targetHosts) {
      subTasks[targetHost.id] = createSubTask(targetHost)
    }

    // Run single host task in parallel on each arch
    script.parallel(subTasks)

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
      if (!host || !host.name || !host.arch || !host.type) {
        script.error "Invalid provisioned host: ${host}"
      }

      // If the provision failed, there will be an error
      if (host.error) {
        script.error(host.error)
      }
    }
  }

  private void teardown(Provisioner provisioner, ProvisionedHost host) {
    try {
      // Ensure teardown runs before the pipeline exits
      script.stage ('Teardown Host') {
        provisioner.teardown(host, arch)
      }
    } catch (e) {
    }
  }

  private void runTask(TargetHost targetHost) {
    // Create an instance of the provisioner
    Provisioner provisioner = new Provisioner(script, config)

    script.node("provisioner-${config.version}") {
      ProvisionedHost host = null
      try {
        host = provision(targetHost)
      } catch (e) {
        onFailure(e, host)
        teardown(provisioner, host)
        return
      }

      if (config.runOnSlave) {
        script.node(host.name) {
          try {
            taskBody(host, config)
          } catch (e) {
            onFailure(e, host)
          }
        }

        teardown(provisioner, host)
        return
      }

      try {
        taskBody(host, config)
      } catch (e) {
        onFailure(e, host)
      } finally {
        teardown(provisioner, host)
      }
    }
  }

  private Closure createSubTask(TargetHost targetHost) {
    def subTask = { target -> { -> runTask(target) } }
    return subTask(targetHost)
  }
}
