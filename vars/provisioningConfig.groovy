import com.redhat.multiarch.ci.ProvisioningConfig

class provisioningConfig {

    def create(params) {
        def config = new ProvisioningConfig()
        config.KEYTABCREDENTIALID = params.KEYTABCREDENTIALID ?: 'KEYTAB'
        config.SSHPRIVKEYCREDENTIALID = params.SSHPRIVKEYCREDENTIALID ?: 'SSHPRIVKEY'
        config.SSHPUBKEYCREDENTIALID = params.SSHPUBKEYCREDENTIALID ?: 'SSHPUBKEY'
        return config
    }
}