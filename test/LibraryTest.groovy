import org.junit.Test
import com.lesfurets.jenkins.unit.BasePipelineTest
import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource
import com.redhat.ci.provisioner.ProvisioningConfig

public class LibraryTest extends BasePipelineTest {

  @Test
  void should_load_library() throws Exception {

    def library = library()
      .name('multiarch-ci-libraries')
      .retriever(localSource(''))
      .targetPath('build/libs')
      .defaultVersion("master")
      .allowOverride(true)
      .implicit(false)
      .build()
    helper.registerSharedLibrary(library)
    printCallStack()
  }

  @Test
  void should_load_configuration() {
    def script = [:]
    script.env = [:]
    script.params = [:]
    ProvisioningConfig config = TestUtils.getProvisioningConfig(script)
  }
}
