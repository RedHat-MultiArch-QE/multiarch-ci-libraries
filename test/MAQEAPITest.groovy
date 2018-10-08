import org.junit.Test
import com.redhat.ci.provisioner.ProvisioningConfig

/**
 * Tests the API wrapper.
 */
class MAQEAPITest extends PipelineTestScript {

    @Test
    void canCallAPIVersion1Methods() {
        ProvisioningConfig config = MAQEAPI.v1.getProvisioningConfig(this)
        assert(config != null)
    }
}
