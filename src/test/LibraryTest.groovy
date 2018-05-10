import com.lesfurets.jenkins.unit.BasePipelineTest

public class LibraryTest extends BasePipelineTest {

  @Test
  void should_load_library() throws Exception {
    String clonePath = 'https://github.com/jaypoulz/multiarch-ci-libraries'

    def library = library()
      .name('multiarch-ci-libraries')
      .retriever(gitSource('git@gitlab.admin.courtanet.net:devteam/lesfurets-jenkins-shared.git'))
      .targetPath(clonePath)
      .defaultVersion("master")
      .allowOverride(true)
      .implicit(false)
      .build()
    helper.registerSharedLibrary(library)
    printCallStack()
  }
}
