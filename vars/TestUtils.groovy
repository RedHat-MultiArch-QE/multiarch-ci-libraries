import com.redhat.multiarch.ci.provisioner.*
import com.redhat.multiarch.ci.test.*
import com.redhat.multiarch.ci.task.*

class TestUtils {
  static ProvisioningConfig config = null

  static ProvisioningConfig getProvisioningConfig(WorkflowScript script) {
    if (!config) config = new ProvisioningConfig(script.params, script.env)
    config
  }

  /**
   * Runs @test on a multi-arch provisioned host for the specified arch.
   * Runs @onTestFailure if it encounters an Exception.
   *
   * @param script WorkflowScript that the test will run in.
   * @param arch String specifying the arch to run tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Host used by the test.
   * @param onTestFailure Closure that take the Host used by the test and the Exception that occured.
   */
  static def runTest(
    WorkflowScript script,
    String arch,
    ProvisioningConfig config,
    Closure test,
    Closure onTestFailure) {
    (new Test(arch, config, test, onTestFailure)).run()
  }

  /**
   * Runs @test on a multi-arch provisioned host for each arch in arches param.
   * Runs @onTestFailure if it encounters an Exception.
   *
   * @param script WorkflowScript that this test will run in.
   * @param arches List<String> specifying the arches to run single host tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Host used by the test.
   * @param onTestFailure Closure that take the Host used by the test and the Exception that occured.
   */
  static def runParallelMultiArchTest(
    WorkflowScript script,
    List<String> arches,
    ProvisioningConfig config,
    Closure test,
    Closure onTestFailure) {
    (new MultiArchTest(script, arches, config, test, onTestFailure)).run()
  }
}
