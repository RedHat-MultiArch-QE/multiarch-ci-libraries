package com.redhat.ci.provisioner

import org.junit.Test

public class ProvisioningConfigTest {

  private static final String KRB_PRINCIPAL_CREDENTIAL_ID_DEFAULT = "test-krbprincipal"
  private static final String KEYTAB_CREDENTIAL_ID_DEFAULT = "test-keytab"
  private static final String SSH_PRIV_KEY_CREDENTIAL_ID_DEFAULT = "test-sshprivkey"
  private static final String SSH_PUB_KEY_CREDENTIAL_ID_DEFAULT = "test-sshpubkey"
  private static final String JENKINS_SLAVE_CREDENTIAL_ID_DEFAULT = "test-jenkins-slave-credentials"
  private static final String JENKINS_MASTER_URL_DEFAULT    = "https://test-jenkins.com";
  private static final String JSWARM_EXTRA_ARGS_DEFAULT     = "-tunnel test-jenkins.com:8080"

  @Test
  void should_support_legacy_api() {
    ProvisioningConfig config = new ProvisioningConfig()
    assert(config.mode == Mode.JNLP)
    config.runOnSlave = false
    assert(config.mode == Mode.SSH)
    config.runOnSlave = true
    assert(config.mode == Mode.JNLP)
    assert(config.runOnSlave == true)
    config.mode = Mode.SSH
    assert(config.runOnSlave == false)
    config.mode = Mode.JNLP
    assert(config.runOnSlave == true)
  }
}
