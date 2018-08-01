import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.Task
import com.redhat.ci.hosts.TargetHost

/**
 * A utility class designed to act as an API to run jobs on provisioned hosts.
 */
@SuppressWarnings('ParameterCount')
class TestUtils {

    /**
     * Generates and retreives a new ProvisioningConfiguration
     * based on a Jenkins script's environment variables and parameters.
     *
     * @param script Script Jenkins script that the configuration belongs to.
     *
     * @return ProvisioningConfig Configuration file for provisioning.
     */
    static ProvisioningConfig getProvisioningConfig(Script script) {
        new ProvisioningConfig(script.params, script.env)
    }

    /**
     * Runs @test on a provisioned host for the specified arch.
     * Runs @onFailure if it encounters an Exception.
     *
     * @param script    Script             Jenkins script that the test will run in.
     * @param arch      String             The arch to run the test on.
     * @param config    ProvisioningConfig Configuration for provisioning.
     * @param test      Closure            Closure that takes the ProvisionedHost used by the test.
     * @param onFailure Closure            Closure that takes the ProvisionedHost used by the test
     *                                     and the Exception that occured.
     * @param postRun   Closure            Closure that is run after the test.
     */
    static void runTest(
        Script script,
        String arch,
        ProvisioningConfig config,
        Closure test,
        Closure onFailure,
        Closure postRun = { })  {
        TargetHost target = new TargetHost()
        target.arch = arch
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
     * Runs @test on a provisioned host for each arch in @arches.
     * Runs @onFailure if it encounters an Exception.
     *
     * @param script    Script             Jenkins script that the test will run in.
     * @param arches    List<String>       List of arches to run test on.
     * @param config    ProvisioningConfig Configuration for provisioning.
     * @param test      Closure            Closure that takes the ProvisionedHost used by the test.
     * @param onFailure Closure            Closure that takes the ProvisionedHost used by the test
     *                                     and the Exception that occured.
     * @param postRun   Closure            Closure that is run after the tests
     */
    static void runParallelMultiArchTest(
        Script script,
        List<String> arches,
        ProvisioningConfig config,
        Closure test,
        Closure onFailure,
        Closure postRun = { }) {
        List<TargetHost> targets = []
        for (arch in arches) {
            targets.push(new TargetHost(arch:arch))
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
     * @param script    Script             Jenkins script that the test will run in.
     * @param target    TargetHost         Host that the test will run on.
     * @param config    ProvisioningConfig Configuration for provisioning.
     * @param test      Closure            Closure that takes the ProvisoinedHost used by the test.
     * @param onFailure Closure            Closure that takes the ProvisionedHost used by the test
     *                                     and the Exception that occured.
     * @param postRun   Closure            Closure that is run after the test.
     */
    static void runTest(
        Script script,
        TargetHost target,
        ProvisioningConfig config,
        Closure test,
        Closure onFailure,
        Closure postRun = { }) {
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
     * @param script    Script             Jenkins script that the test will run in.
     * @param hosts     List<TargetHost>   List of specifications for target hosts that the test will run on.
     * @param config    ProvisioningConfig Configuration for provisioning.
     * @param test      Closure            Closure that takes the ProvisionedHost used by the test.
     * @param onFailure Closure            Closure that takes the Provisioned used by the test
     *                                     and the Exception that occured.
     * @param postRun   Closure            Closure that is run after the tests.
     */
    static void runTest(
        Script script,
        List<TargetHost> targets,
        ProvisioningConfig config,
        Closure test,
        Closure onFailure,
        Closure postRun = { }) {
        Closure jobRunner = {
            Task job = new Task(script, targets, config, test, onFailure, postRun)
            job.run()
        }
        testWrapper(
            script,
            config,
            jobRunner
        )
    }

    /**
     * Runs @test in ProvisioningConfig defined container as part of a Jenkins script.
     *
     * @param script Script             Jenkins script the test wrapper will run in.
     * @param config ProvisioningConfig Configuration Defining the container.
     * @param test   Closure            Closure that will run on the container.
     */
    @SuppressWarnings('GStringExpressionWithinString')
    static void testWrapper(Script script, ProvisioningConfig config, Closure test) {
        script.podTemplate(
            name:"provisioner-${config.version}",
            label:"provisioner-${config.version}",
            cloud:config.cloudName,
            serviceAccount:'jenkins',
            idleMinutes:0,
            namespace:config.tenant,
            containers:[
                // This adds the custom provisioner slave container to the pod. Must be first with name 'jnlp'
                script.containerTemplate(
                    name:'jnlp',
                    image:"${config.dockerUrl}/${config.tenant}/${config.provisioningImage}-${config.version}",
                    ttyEnabled:false,
                    args:'${computer.jnlpmac} ${computer.name}',
                    command:'',
                    workingDir:'/tmp'
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
