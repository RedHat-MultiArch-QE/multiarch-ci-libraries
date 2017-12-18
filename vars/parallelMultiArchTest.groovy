/**
 * parallelMultiArchTest.groovy
 *
 * Runs closure body on a multi-arch slave for each arch in arches param.
 *
 * @param arches List<String> specifying the arches to run single host tests on.
 * @param config ProvisioningConfig Configuration for provisioning.
 * @param test Closure that takes the Slave used by the test.
 * @param onTestFailure Closure that takes the Slave used by the test and the Exception that occured.
 */
import com.redhat.multiarch.ci.Task
import com.redhat.multiarch.ci.ProvisioningConfig

def call(List<String> arches,
         ProvisioningConfig config,
         Closure test,
         Closure onTestFailure) {

  // Create arch Tasks to parallelize test
  def parallelTasks = []
  for (arch in arches) {
    parallelTasks.push(new Task(name: arch, params: [ arch: arch ]))
  }

  // Run single host test in parallel on each arch
  parallelizeTasks(
    parallelTasks,
    { params ->
        String arch = params.arch
        println arch
        return {
            runTest(arch,
                    config,
                    test,
                    onTestFailure)
        }
    }
  )
}
