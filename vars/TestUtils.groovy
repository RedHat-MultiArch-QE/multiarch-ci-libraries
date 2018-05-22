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
   * @param postTest Closure that is run after the tests
   */
  static def runTest(
    WorkflowScript script,
    String arch,
    ProvisioningConfig config,
    Closure test,
    Closure onTestFailure,
    Closure postTest = {}) {
    TestUtils.testWrapper(
      script,
      config,
      {
        (new Test(arch, config, test, onTestFailure, postTest)).run()
      }
    )
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
   * @param postTest Closure that is run after the tests
   */
  static def runParallelMultiArchTest(
    WorkflowScript script,
    List<String> arches,
    ProvisioningConfig config,
    Closure test,
    Closure onTestFailure,
    Closure postTest = {}) {
    TestUtils.testWrapper(
      script,
      config,
      {
        (new MultiArchTest(script, arches, config, test, onTestFailure, postTest)).run()
      }
    )
  }

  static def testWrapper(WorkflowScript script, ProvisioningConfig config, Closure test) {
    script.podTemplate(
      name: "provisioner-${config.version}",
      label: "provisioner-${config.version}",
      cloud: config.cloudName,
      serviceAccount: 'jenkins',
      idleMinutes: 0,
      namespace: config.tenant,
      containers: [
        // This adds the custom provisioner slave container to the pod. Must be first with name 'jnlp'
        script.containerTemplate(
          name: 'jnlp',
          image: "${config.dockerUrl}/${config.tenant}/${config.provisioningImage}-${config.version}",
          ttyEnabled: false,
          args: '${computer.jnlpmac} ${computer.name}',
          command: '',
          workingDir: '/tmp',
          privileged: true
        )
      ]
    ) {
      script.ansiColor('xterm') {
        script.timestamps {
          script.node("provisioner-${config.version}") {
            test()
          }
        }
      }
    }
  }
}
