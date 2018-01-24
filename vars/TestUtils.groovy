import com.redhat.multiarch.ci.provisioner.*
import com.redhat.multiarch.ci.test.*

class TestUtils {
  static ProvisioningConfig createProvisioningConfig(script) {
    return new ProvisioningConfig(script.params, script.env)
  }

  /**
   * Runs @test on the multi-arch capable provisioner container,
   * an runs @onTestFailure if it encounter an Exception.
   *
   * @param arch String specifying the arch to run tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Slave used by the test.
   * @param onTestFailure Closure that take the Slave used by the test and the Exception that occured.
   */
  static def runTest(String arch,
       ProvisioningConfig config,
       Closure test,
       Closure onTestFailure) {
    (new Test(arch, config, test, onTestFailure)).run()
  }

  /**
   * Run closure body on a multi-arch provisioned host for each arch in arches param.
   *
   * @param arches List<String> specifying the arches to run single host tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Slave used by the test.
   * @param onTestFailure Closure that take the Slave used by the test and the Exception that occured.
   */
  static def runParallelMultiArchTest(List<String> arches,
       ProvisioningConfig config,
       Closure test,
       Closure onTestFailure) {
    (new ParallelMultiArchTest(arches, config, test, onTestFailure)).run()
  }
}
