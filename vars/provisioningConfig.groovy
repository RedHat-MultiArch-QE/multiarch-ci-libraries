import com.redhat.multiarch.ci.ProvisioningConfig

class provisioningConfig {

    def create(params) {
        def config = new ProvisioningConfig()
        config.KEYTABCREDENTIALID = params.KEYTABCREDENTIALID ?: config.KEYTABCREDENTIALID
        config.SSHPRIVKEYCREDENTIALID = params.SSHPRIVKEYCREDENTIALID ?: config.SSHPRIVKEYCREDENTIALID
        config.SSHPUBKEYCREDENTIALID = params.SSHPUBKEYCREDENTIALID ?: config.SSHPUBKEYCREDENTIALID
        config.JENKINSSLAVECREDENTIALID = params.JENKINSSLAVECREDENTIALID ?: config.JENKINSSLAVECREDENTIALID
        return config
    }
}
