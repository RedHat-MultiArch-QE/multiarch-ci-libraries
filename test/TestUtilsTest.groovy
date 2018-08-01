import org.junit.Test
import org.junit.Before
import com.redhat.ci.provisioner.ProvisioningConfig
import java.util.logging.Logger

/**
 * Tests TestUtils API and whether or not a library is importable.
 */
class TestUtilsTest extends Script {
    private static final Logger LOG = Logger.getLogger('TestUtilsTest')
    private final Binding binding = new Binding()

    @Before
    void setUp() {
        setBinding(binding)
        binding.with {
            params = [:]
            env = [:]
            ansiColor = {
                string, body ->
                LOG.info("ansiColor(${string})")
                body()
            }
            podTemplate = {
                map, body ->
                LOG.info('podTemplate()')
                body()
            }
            containerTemplate = {
                map ->
                LOG.info('containerTemplate()')
            }
            timestamps = {
                body ->
                LOG.info('timestamps()')
                body()
            }
        }
    }

    @Test
    void shouldGetProvisioningConfig() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        assert(config != null)
    }

    @SuppressWarnings('JUnitPublicNonTestMethod')
    Object run() {
        null
    }

    @Test
    void shouldRunTestOnSingleHost() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TestUtils.runTest(this, 'x86_64', config, { }, { }) { }
        assert(config != null)
    }

    @Test
    void shouldRunTestOnMultiArchHosts() {
        ProvisioningConfig config = TestUtils.getProvisioningConfig(this)
        TestUtils.runParallelMultiArchTest(this, ['x86_64', 'ppc64le', 'aarch64', 's390x'], config, { }, { }) { }
        assert(config != null)
    }
}
