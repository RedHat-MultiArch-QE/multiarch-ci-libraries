import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mock
import com.redhat.ci.host.Type
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.ProvisioningConfig
import java.util.logging.Logger

/**
 * Tests TestUtils API and whether or not a library is importable.
 */
@RunWith(MockitoJUnitRunner)
class TestUtilsTest extends PipelineTestScript {
    private static final Logger LOG = Logger.getLogger('TestUtilsTest')
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
        throw new RuntimeException()
    }

    private final Closure onFailure = {
        Exception e, ProvisionedHost host ->
        LOG.info('onFailure(e, host)')
        LOG.severe(e.toString())
        LOG.severe("Failed on host ${host.id} with exception")
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
        TargetHost target = new TargetHost(arch:X86_64, type:Type.BAREMETAL)
        TestUtils.runTest(this, target, config, body, onFailure, onComplete)
        assert(config != null)
    }

    @Test
    void shouldRunTestOnVMHost() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TargetHost target = new TargetHost(arch:X86_64, type:Type.VM)
        TestUtils.runTest(this, target, config, body, onFailure, onComplete)
        assert(config != null)
    }

    @Test
    void shouldFailOnRun() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TestUtils.runTest(this, 'x86_64', config, errorBody, onFailure, onComplete)
        assert(config != null)
    }

    @Test
    @ExpectedException
    void shouldThrowException() {
    }
}
