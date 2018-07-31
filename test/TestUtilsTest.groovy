import org.junit.Test
import org.junit.Before
import com.redhat.ci.provisioner.ProvisioningConfig

/**
 * Tests TestUtils API and whether or not a library is importable.
 */
class TestUtilsTest extends Script {
    private final Binding binding = new Binding()

    @Before
    void setUp() {
        setBinding(binding)
        binding.setProperty('params', [:])
        binding.setProperty('env', [:])
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
}
