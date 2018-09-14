import org.junit.Test
import com.redhat.ci.provisioner.ProvisioningConfig

/**
 * Tests the API wrapper.
 */
class APITest extends PipelineTestScript {

    @Test
    void canCallAPIVersion1Methods() {
        ProvisioningConfig config = API.v1.getProvisioningConfig(this)
        assert(config != null)
    }
}
