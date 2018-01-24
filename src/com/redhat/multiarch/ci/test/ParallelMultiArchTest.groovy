package com.redhat.multiarch.ci.test

import com.redhat.multiarch.ci.task.*

class ParallelMultiArchTest extends Test {

  List<String> arches

  /**
   * @param arches List<String> specifying the arches to run single host tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Host to be used by the test.
   * @param onTestFailure Closure that takes the Host used by the test and the Exception that occured.
   */
  ParallelMultiArchTest(List<String> arches,
                        ProvisioningConfig config,
                        Closure test,
                        Closure onTestFailure) {
    super(null, config, test, onTestFailure)
    this.arches = arches
  }

  /**
   * Runs closure body on a multi-arch provisioned host for each arch in arches param.
   */
  def run() {
    // Create arch Tasks to parallelize test
    def parallelTasks = []
    for (arch in arches) {
      parallelTasks.push(new Task(name: arch, params: [ arch: arch ]))
    }

    // Run single host test in parallel on each arch
    parallelizeTasks(
      parallelTasks,
      { params ->
        this.arch = params.arch
        return {
          super.run()
        }
      }
    )
  }
}
