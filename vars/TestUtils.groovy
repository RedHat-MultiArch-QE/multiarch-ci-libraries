import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.Task
import com.redhat.ci.hosts.TargetHost

class TestUtils {

  /**
   * Generates and retreives a new ProvisioningConfiguration 
   * based on a Jenkins script's environment variables and parameters.
   *
   * @param script def Jenkins script that the configuration belongs to.
   *
   * @return ProvisioningConfig Configuration file for provisioning.
   */
  static ProvisioningConfig getProvisioningConfig(def script) {
      new ProvisioningConfig(script.params, script.env)
  }

  /**
   * Runs @test on a provisioned host for the specified arch.
   * Runs @onFailure if it encounters an Exception.
   *
   * @param script    def                Jenkins script that the test will run in.
   * @param arch      String             The arch to run the test on.
   * @param config    ProvisioningConfig Configuration for provisioning.
   * @param test      Closure            Closure that takes the ProvisionedHost used by the test.
   * @param onFailure Closure            Closure that takes the ProvisionedHost used by the test and the Exception that occured.
   * @param postRun   Closure            Closure that is run after the test.
   */
  static void runTest(
    def script,
    String arch,
    ProvisioningConfig config,
    Closure test,
    Closure onFailure,
    Closure postRun = {})  {
    TargetHost target = new TargetHost()
    target.arch = arch
    TestUtils.runTest(
      script,
      [ target ],
      config,
      test,
      onFailure,
      postRun
    )
  }

  /**
   * Runs @test on a provisioned host for each arch in @arches.
   * Runs @onFailure if it encounters an Exception.
   *
   * @param script    def                Jenkins script that the test will run in.
   * @param arches    List<String>       List of arches to run test on.
   * @param config    ProvisioningConfig Configuration for provisioning.
   * @param test      Closure            Closure that takes the ProvisionedHost used by the test.
   * @param onFailure Closure            Closure that takes the ProvisionedHost used by the test and the Exception that occured.
   * @param postRun   Closure            Closure that is run after the tests
   */
  static void runParallelMultiArchTest(
    def script,
    List<String> arches,
    ProvisioningConfig config,
    Closure test,
    Closure onFailure,
    Closure postRun = {}) {
    List<TargetHost> targets = []
    for (arch in arches) {
      targets.push(new TargetHost(arch: arch))
    }
    runTest(
      script,
      targets,
      config,
      test,
      onFailure,
      postRun
    )
  }

  /**
   * Runs @test on a provisioned host for the specified arch.
   * Runs @onFailure if it encounters an Exception.
   *
   * @param script    def                Jenkins script that the test will run in.
   * @param target    TargetHost         Host that the test will run on.
   * @param config    ProvisioningConfig Configuration for provisioning.
   * @param test      Closure            Closure that takes the ProvisoinedHost used by the test.
   * @param onFailure Closure            Closure that takes the ProvisionedHost used by the test and the Exception that occured.
   * @param postRun   Closure            Closure that is run after the test.
   */
  static void runTest(
    def script,
    TargetHost target,
    ProvisioningConfig config,
    Closure test,
    Closure onFailure,
    Closure postRun = {}) {
    runTest(
      script,
      [ target ],
      config,
      test,
      onFailure,
      postRun
    )
  }

  /**
   * Runs @test on a provisioned host for each specified TargetHost.
   * Runs @onFailure if it encounters an Exception.
   *
   * @param script    def                Jenkins script that the test will run in.
   * @param hosts     List<TargetHost>   List of specifications for target hosts that the test will run on.
   * @param config    ProvisioningConfig Configuration for provisioning.
   * @param test      Closure            Closure that takes the ProvisionedHost used by the test.
   * @param onFailure Closure            Closure that takes the Provisioned used by the test and the Exception that occured.
   * @param postRun   Closure            Closure that is run after the tests.
   */
  static void runTest(
    def script,
    List<TargetHost> targets,
    ProvisioningConfig config,
    Closure test,
    Closure onFailure,
    Closure postRun = {}) {
    TestUtils.testWrapper(
      script,
      config,
      {
        (new Task(script, targets, config, test, onFailure, postRun)).run()
      }
    )
  }

  /**
   * Runs @test in ProvisioningConfig defined container as part of a Jenkins script.
   *
   * @param script def                Jenkins script the test wrapper will run in.
   * @param config ProvisioningConfig Configuration defining the container.
   * @param test   Closure            Closure that will run on the container.
   */
  static void testWrapper(def script, ProvisioningConfig config, Closure test) {
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
          workingDir: '/tmp'
        )
      ]
    ) {
      script.ansiColor('xterm') {
        script.timestamps {
          test()
        }
      }
    }
  }
}
