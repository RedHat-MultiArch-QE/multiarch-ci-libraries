import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource
import org.junit.Test
import org.junit.Before
import com.lesfurets.jenkins.unit.BasePipelineTest
import com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration

/**
 * Tests TestUtils API and whether or not a library is importable.
 */
class TestUtilsTest extends BasePipelineTest {

    private LibraryConfiguration library = null

    @Before
    void setUp() {
        library = library()
            .name('multiarch-ci-libraries')
            .retriever(localSource('build/libs'))
            .targetPath('build/libs')
            .defaultVersion('master')
            .allowOverride(false)
            .implicit(false)
            .build()
        helper.registerSharedLibrary(library)

        helper.baseScriptRoot = 'test/jobs/'
        helper.scriptRoots += 'vars'
        helper.registerAllowedMethod('checkout', [LinkedHashMap]) { m -> }
        super.setUp()
        binding.setVariable('params', [:])
        binding.setVariable('env', [:])
        binding.setVariable('scm', [:])
    }

    @Test
    void shouldLoadLibrary() {
        assert(library != null)
    }

    @Test
    void canRunBasicPipeline() {
        runScript('test/jobs/basicPipeline.jenkins')
        printCallStack()
        assertJobStatusSuccess()
    }
}
