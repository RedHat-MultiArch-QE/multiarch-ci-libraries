import org.junit.Test
import com.lesfurets.jenkins.unit.BasePipelineTest
import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.ProvisioningConfigTest

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
  void should_load_default_configuration() {
    def script = [:]
    script.params = [:]
    script.env = [:]
    ProvisioningConfig config = TestUtils.getProvisioningConfig(script)
    assert(config.krbPrincipalCredentialId == ProvisioningConfig.KRB_PRINCIPAL_CREDENTIAL_ID_DEFAULT)
    assert(config.keytabCredentialId       == ProvisioningConfig.KEYTAB_CREDENTIAL_ID_DEFAULT)
    assert(config.sshPrivKeyCredentialId   == ProvisioningConfig.SSH_PRIV_KEY_CREDENTIAL_ID_DEFAULT)
    assert(config.sshPubKeyCredentialId    == ProvisioningConfig.SSH_PUB_KEY_CREDENTIAL_ID_DEFAULT)
    assert(config.jenkinsSlaveCredentialId == ProvisioningConfig.JENKINS_SLAVE_CREDENTIAL_ID_DEFAULT)
    assert(config.jenkinsMasterUrl         == ProvisioningConfig.JENKINS_MASTER_URL_DEFAULT)
    assert(config.jswarmExtraArgs          == ProvisioningConfig.JSWARM_EXTRA_ARGS_DEFAULT)
  }

  @Test
  void should_load_customized_configuration() {
    def script = [:]
    script.params = [:]
    script.env = [:]
    script.params.KRBPRINCIPALCREDENTIALID = ProvisioningConfigTest.KRB_PRINCIPAL_CREDENTIAL_ID_DEFAULT
    script.params.KEYTABCREDENTIALID       = ProvisioningConfigTest.KEYTAB_CREDENTIAL_ID_DEFAULT
    script.params.SSHPRIVKEYCREDENTIALID   = ProvisioningConfigTest.SSH_PRIV_KEY_CREDENTIAL_ID_DEFAULT
    script.params.SSHPUBKEYCREDENTIALID    = ProvisioningConfigTest.SSH_PUB_KEY_CREDENTIAL_ID_DEFAULT
    script.params.JENKINSSLAVECREDENTIALID = ProvisioningConfigTest.JENKINS_SLAVE_CREDENTIAL_ID_DEFAULT
    script.env.JENKINS_MASTER_URL          = ProvisioningConfigTest.JENKINS_MASTER_URL_DEFAULT
    script.env.JSWARM_EXTRA_ARGS           = ProvisioningConfigTest.JSWARM_EXTRA_ARGS_DEFAULT
    
    ProvisioningConfig config = TestUtils.getProvisioningConfig(script)
    assert(config.krbPrincipalCredentialId == ProvisioningConfigTest.KRB_PRINCIPAL_CREDENTIAL_ID_DEFAULT)
    assert(config.keytabCredentialId       == ProvisioningConfigTest.KEYTAB_CREDENTIAL_ID_DEFAULT)
    assert(config.sshPrivKeyCredentialId   == ProvisioningConfigTest.SSH_PRIV_KEY_CREDENTIAL_ID_DEFAULT)
    assert(config.sshPubKeyCredentialId    == ProvisioningConfigTest.SSH_PUB_KEY_CREDENTIAL_ID_DEFAULT)
    assert(config.jenkinsSlaveCredentialId == ProvisioningConfigTest.JENKINS_SLAVE_CREDENTIAL_ID_DEFAULT)
    assert(config.jenkinsMasterUrl         == ProvisioningConfigTest.JENKINS_MASTER_URL_DEFAULT)
    assert(config.jswarmExtraArgs          == ProvisioningConfigTest.JSWARM_EXTRA_ARGS_DEFAULT)
  }
}
