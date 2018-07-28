import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource
import org.junit.Test
import com.lesfurets.jenkins.unit.BasePipelineTest
import com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration

/**
 * Tests TestUtils API and whether or not a library is importable.
 */
class TestUtilsTest extends BasePipelineTest {

    @Test
    void should_load_library() {
        LibraryConfiguration library = library()
            .name('multiarch-ci-libraries')
            .retriever(localSource(''))
            .targetPath('build/libs')
            .defaultVersion('master')
            .allowOverride(true)
            .implicit(false)
            .build()
        helper.registerSharedLibrary(library)
        printCallStack()

        assert(library != null)
    }
}
