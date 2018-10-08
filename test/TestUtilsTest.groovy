import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mock
import com.redhat.ci.host.Type
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.Mode
import com.redhat.ci.provisioner.ProvisioningException
import java.util.logging.Logger
import java.util.logging.Level

/**
 * Tests TestUtils API and whether or not a library is importable.
 */

@RunWith(MockitoJUnitRunner)
class TestUtilsTest extends PipelineTestScript {

    private static final Logger LOG = Logger.getLogger(TestUtilsTest.name)
    private static final String X86_64 = 'x86_64'
    private static final List<String> TEST_MODES = [Mode.SSH, Mode.JNLP]

    @Mock
    private final ProvisionedHost host

    private final Closure body = {
        ProvisionedHost host, ProvisioningConfig config ->
        LOG.info('body(host, config)')
        echo("Running on host ${host.id}")
        assert(host)
    }

    private final Closure errorBody = {
        ProvisionedHost host, ProvisioningConfig config ->
        LOG.info('body(host, config)')
        echo("Running on host ${host.id}, and throwing Exception")
        throw new TestException()
    }

    private final Closure onFailure = {
        Exception e, ProvisionedHost host ->
        LOG.info('onFailure(e, host)')
        echo(e.message)
        LOG.log(Level.SEVERE, "Failed on host ${host} with exception", e)
        throw e
    }

    private final Closure onComplete = {
        ->
        LOG.info('onComplete()')
    }

    @Before
    void init() {
        reset()
    }

    @Test
    void shouldGetProvisioningConfig() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        assert(config)
        assertNoExceptions()
    }

    @Test
    void shouldGetProvisioningConfigViaAPI() {
        ProvisioningConfig config = MAQEAPI.v1.getProvisioningConfig(this)
        assert(config)
        assertNoExceptions()
    }

    @Test
    void shouldRunTestOnSingleHost() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TestUtils.runTest(this, X86_64, config, body, onFailure, onComplete)
        assertNoExceptions()
    }

    @Test
    void shouldRunTestOnMultiArchHosts() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TestUtils.runParallelMultiArchTest(
            this,
            [X86_64, 'ppc64le', 'aarch64', 's390x'],
            config,
            body,
            onFailure,
            onComplete)

        assertNoExceptions()
    }

    @Test
    void shouldRunTestOnBareMetalHost() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TargetHost target = new TargetHost(arch:X86_64, type:Type.BAREMETAL)
        TestUtils.runTest(this, target, config, body, onFailure, onComplete)
        assertNoExceptions()
    }

    @Test
    void shouldRunTestOnVMHost() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TargetHost target = new TargetHost(
            arch:X86_64,
            type:Type.VM,
            provisioner:com.redhat.ci.provisioner.Type.LINCHPIN,
            provider:com.redhat.ci.provider.Type.BEAKER
        )
        TestUtils.runTest(this, target, config, body, onFailure, onComplete)
        assertNoExceptions()
    }

    @Test
    void shouldInstallAllConfigurationTest() {
        TEST_MODES.each {
            mode ->
            ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
            config.mode = mode
            config.installAnsible = true
            config.installRhpkg = true

            TargetHost target = new TargetHost(arch:X86_64)
            TestUtils.runTest(this, target, config, body, onFailure, onComplete)
            assertNoExceptions()
            reset()
        }
    }

    @Test
    void shouldFailOnRun() {
        TEST_MODES.each {
            mode ->
            Boolean exceptionOccured = false
            try {
                ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
                config.mode = mode

                TestUtils.runTest(this, X86_64, config, errorBody, onFailure, onComplete)
            } catch (TestException e) {
                LOG.severe("${e.message}")
                exceptionOccured = true
            }
            assert(exceptionOccured)
        }
    }

    @Test
    void shouldFailWithNoProvisionerAvailable() {
        ProvisioningConfig config = MAQEAPI.v1.getProvisioningConfig(this)

        TargetHost target = new TargetHost(arch:X86_64, provisionerPriority:[])
        Boolean exceptionOccured = false
        try {
            TestUtils.runTest(this, target, config, body, onFailure, onComplete)
        } catch (ProvisioningException e) {
            exceptionOccured = true
        }

        assert(exceptionOccured)
    }

    private void assertNoExceptions() {
        testLog.each {
            msg ->
            assert(!msg.toLowerCase().contains('exception'))
        }
    }
}
