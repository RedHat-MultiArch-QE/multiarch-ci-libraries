package com.redhat.ci.provisioner

import org.junit.Test

/**
 * Tests backwards compatibility of the ProvisioningConfig.
 */
class ProvisioningConfigTest {

    private static final String KRB_PRINCIPAL_CREDENTIAL_ID_DEFAULT = 'test-krbprincipal'
    private static final String KEYTAB_CREDENTIAL_ID_DEFAULT = 'test-keytab'
    private static final String SSH_PRIV_KEY_CREDENTIAL_ID_DEFAULT = 'test-sshprivkey'
    private static final String SSH_PUB_KEY_CREDENTIAL_ID_DEFAULT = 'test-sshpubkey'
    private static final String JENKINS_SLAVE_CREDENTIAL_ID_DEFAULT = 'test-jenkins-slave-credentials'
    private static final String JENKINS_MASTER_URL_DEFAULT    = 'https://test-jenkins.com'
    private static final String JSWARM_EXTRA_ARGS_DEFAULT     = '-tunnel test-jenkins.com:8080'

    @Test
    void should_support_legacy_api() {
        ProvisioningConfig config = new ProvisioningConfig()
        assert(config.mode == Mode.SSH)
        config.runOnSlave = true
        assert(config.mode == Mode.JNLP)
        config.runOnSlave = false
        assert(config.mode == Mode.SSH)
        assert(config.runOnSlave == false)
        config.mode = Mode.JNLP
        assert(config.runOnSlave == true)
        config.mode = Mode.SSH
        assert(config.runOnSlave == false)
    }

    @Test
    void should_load_default_configuration() {
        Map script = [:]
        script.params = [:]
        script.env = [:]
        ProvisioningConfig config = new ProvisioningConfig(script.params, script.env)
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
        Map script = [:]
        script.params = [:]
        script.env = [:]
        script.params.KRBPRINCIPALCREDENTIALID = KRB_PRINCIPAL_CREDENTIAL_ID_DEFAULT
        script.params.KEYTABCREDENTIALID       = KEYTAB_CREDENTIAL_ID_DEFAULT
        script.params.SSHPRIVKEYCREDENTIALID   = SSH_PRIV_KEY_CREDENTIAL_ID_DEFAULT
        script.params.SSHPUBKEYCREDENTIALID    = SSH_PUB_KEY_CREDENTIAL_ID_DEFAULT
        script.params.JENKINSSLAVECREDENTIALID = JENKINS_SLAVE_CREDENTIAL_ID_DEFAULT
        script.env.JENKINS_MASTER_URL          = JENKINS_MASTER_URL_DEFAULT
        script.env.JSWARM_EXTRA_ARGS           = JSWARM_EXTRA_ARGS_DEFAULT

        ProvisioningConfig config = new ProvisioningConfig(script.params, script.env)
        assert(config.krbPrincipalCredentialId == KRB_PRINCIPAL_CREDENTIAL_ID_DEFAULT)
        assert(config.keytabCredentialId       == KEYTAB_CREDENTIAL_ID_DEFAULT)
        assert(config.sshPrivKeyCredentialId   == SSH_PRIV_KEY_CREDENTIAL_ID_DEFAULT)
        assert(config.sshPubKeyCredentialId    == SSH_PUB_KEY_CREDENTIAL_ID_DEFAULT)
        assert(config.jenkinsSlaveCredentialId == JENKINS_SLAVE_CREDENTIAL_ID_DEFAULT)
        assert(config.jenkinsMasterUrl         == JENKINS_MASTER_URL_DEFAULT)
        assert(config.jswarmExtraArgs          == JSWARM_EXTRA_ARGS_DEFAULT)
    }
}
