import com.redhat.multiarch.ci.provisioner.*
import com.redhat.multiarch.ci.test.*

class TestUtils {
  static def config
  
  static ProvisioningConfig getProvisioningConfig(script) {
    if (provisioningConfig) return provisioningConfig
    config = new ProvisioningConfig(script.params, script.env)
  }

  /**
   * Runs @test on the multi-arch capable provisioner container,
   * an runs @onTestFailure if it encounter an Exception.
   *
   * @param script Context for calling pipeline steps.
   * @param arch String specifying the arch to run tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Slave used by the test.
   * @param onTestFailure Closure that take the Slave used by the test and the Exception that occured.
   */
  static def runTest(
    def script,
    String arch,
    ProvisioningConfig config,
    Closure test,
    Closure onTestFailure) {
    (new Test(this, arch, config, test, onTestFailure)).run()
  }

  /**
   * Run closure body on a multi-arch provisioned host for each arch in arches param.
   *
   * @param script Context for calling pipeline steps.
   * @param arches List<String> specifying the arches to run single host tests on.
   * @param config ProvisioningConfig Configuration for provisioning.
   * @param test Closure that takes the Slave used by the test.
   * @param onTestFailure Closure that take the Slave used by the test and the Exception that occured.
   */
  static def runParallelMultiArchTest(
    def script,
    List<String> arches,
    ProvisioningConfig config,
    Closure test,
    Closure onTestFailure) {
      (new ParallelMultiArchTest(this, arches, config, test, onTestFailure)).run()
    }
  }
