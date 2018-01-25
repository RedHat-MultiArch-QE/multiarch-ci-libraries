package com.redhat.multarch.ci.test

import com.redhat.multiarch.ci.provisioner.Provisioner
import com.redhat.multiarch.ci.provisioner.ProvisioningConfig
import com.redhat.multiarch.ci.task.Task
import com.redhat.multiarch.ci.test.Test

class MultiArchTest extends Test {
  List<String> arches

  /**
   * @param script WorkflowScript that the test will run in.
   * @param arch String specifying the arch to run tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Slave used by the test.
   * @param onTestFailure Closure that take the Slave used by the test and the Exception that occured.
   */
  MultiArchTest(def script, List<String> arches, ProvisioningConfig config,
                Closure test, Closure onTestFailure) {
    super(script, null, config, test, onTestFailure)
    this.arches = arches
  }

  /**
   * Runs @test on a multi-arch provisioned host for each arch in arches param.
   * Runs @onTestFailure if it encounters an Exception.
   */
  def run() {
    // Create arch Tasks to parallelize test
    def parallelTasks = []
    for (arch in arches) {
      parallelTasks.push(new Task(name: arch, params: [ arch: arch ]))
    }

    // Run single host test in parallel on each arch
    script.parallel Task.parallelizeTaskList(
      parallelTasks,
      { params ->
        def arch = params.arch
        return {
          (new Test(arch, config, test, onTestFailure)).run()
        }
      }
    )
  }
}
