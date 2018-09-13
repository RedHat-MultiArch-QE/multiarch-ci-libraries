import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mock
import com.redhat.ci.host.Type
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.ProvisioningService
import java.util.logging.Logger
import java.util.logging.Level

/**
 * Tests TestUtils API and whether or not a library is importable.
 */
@RunWith(MockitoJUnitRunner)
class TestUtilsTest extends PipelineTestScript {

    private static final Logger LOG = Logger.getLogger(TestUtilsTest.name)
    private static final String X86_64 = 'x86_64'

    @Mock
    private final ProvisionedHost host

    private final Closure body = {
        ProvisionedHost host, ProvisioningConfig config ->
        LOG.info('body(host, config)')
        LOG.info("Running on host ${host.id}")
    }

    @SuppressWarnings('ThrowRuntimeException')
    private final Closure errorBody = {
        ProvisionedHost host, ProvisioningConfig config ->
        LOG.info('body(host, config)')
        LOG.info("Running on host ${host.id}, and throwing Exception")
        throw new TestException()
    }

    private final Closure onFailure = {
        Exception e, ProvisionedHost host ->
        LOG.info('onFailure(e, host)')
        LOG.severe(e.toString())
        LOG.log(Level.SEVERE, "Failed on host ${host} with exception", e)
        throw e
    }

    private final Closure onComplete = {
        ->
        LOG.info('onComplete()')
    }

    @Test
    void shouldGetProvisioningConfig() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        assert(config != null)
    }

    @Test
    void shouldRunTestOnSingleHost() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TestUtils.runTest(this, X86_64, config, body, onFailure, onComplete)
        assert(config != null)
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
        assert(config != null)
    }

    @Test
    void shouldRunTestOnBareMetalHost() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TargetHost target = new TargetHost(arch:'x86_64', type:Type.BAREMETAL)
        TestUtils.runTest(this, target, config, body, onFailure, onComplete)
        assert(config != null)
    }

    @Test
    void shouldRunTestOnVMHost() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TargetHost target = new TargetHost(
            arch:'x86_64',
            type:Type.VM,
            provisioner:com.redhat.ci.provisioner.Type.LINCHPIN,
            provider:com.redhat.ci.provider.Type.BEAKER
        )
        TestUtils.runTest(this, target, config, body, onFailure, onComplete)
        assert(config != null)
    }

    @Test
    void shouldInstallAllConfigurationTest() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        config.runOnSlave = true
        config.installAnsible = true
        config.installRhpkg = true

        TargetHost target = new TargetHost(arch:'x86_64')
        TestUtils.runTest(this, target, config, body, onFailure, onComplete)
        assert(config != null)
    }

    @Test
    void shouldFailOnRun() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        Boolean exceptionOccured = false
        try {
            TestUtils.runTest(this, 'x86_64', config, errorBody, onFailure, onComplete)
        } catch (TestException e) {
            exceptionOccured = true
        }
        assert(exceptionOccured)
    }

    @Test
    void shouldFailWithNoProvisionerAvailable() {
        ProvisioningConfig config = API.v1.getProvisioningConfig(this)

        TargetHost target = new TargetHost(arch:'x86_64', provisionerPriority:[])
        Boolean exceptionOccured = false
        try {
            TestUtils.runTest(this, target, config, body, onFailure, onComplete)
        } catch (ProvisioningService.ProvisionerUnavailableException e) {
            exceptionOccured = true
        }

        assert(exceptionOccured)
    }
}
