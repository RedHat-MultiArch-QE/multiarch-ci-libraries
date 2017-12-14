/**
 * parallelMultiArchTest.groovy
 *
 * Runs closure body on a multi-arch slave for each arch in arches param.
 *
 * @param arches List<String> specifying the arches to run single host tests on.
 * @param runOnSlave Boolean that specificies whether the
 *        closure should be run on directly on the provisioned slave.
 * @param installAnsible Boolean that specificies whether Ansible should
 *        be installed on the provisioned slave.
 * @param test Closure that takes the Slave used by the test.
 * @param onTestFailure Closure that takes the Slave used by the test and the Exception that occured.
 */
import com.redhat.multiarch.ci.Task

def call(List<String> arches, Boolean runOnSlave, Boolean installAnsible, Closure test, Closure onTestFailure) {
  // Create arch Tasks to parallelize test
  def LinkedList<Task> parallelTasks = []
  for (arch in arches) {
    parallelTasks.push(new Task(name: arch, params: { arch: arch }))
  }

  // Run single host test in parallel on each arch
  parallelizeTasks(
    parallelTasks,
    { a ->
      String arch = new String(a)
      {
        runTest(arch, runOnSlave, installAnsible, test, onTestFailure)
      }
    }
  )
}
