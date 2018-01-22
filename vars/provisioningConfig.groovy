import com.redhat.multiarch.ci.ProvisioningConfig

class provisioningConfig {

    def create(params, extras) {
        def config = new ProvisioningConfig()
        config.KEYTABCREDENTIALID = params.KEYTABCREDENTIALID ?: config.KEYTABCREDENTIALID
        config.SSHPRIVKEYCREDENTIALID = params.SSHPRIVKEYCREDENTIALID ?: config.SSHPRIVKEYCREDENTIALID
        config.SSHPUBKEYCREDENTIALID = params.SSHPUBKEYCREDENTIALID ?: config.SSHPUBKEYCREDENTIALID
        config.JENKINSSLAVECREDENTIALID = params.JENKINSSLAVECREDENTIALID ?: config.JENKINSSLAVECREDENTIALID
        config.JENKINS_MASTER_URL = extras.JENKINS_MASTER_URL ?: config.JENKINS_MASTER_URL
        config.JSWARM_EXTRA_ARGS = extras.JSWARM_EXTRA_ARGS ?: config.JSWARM_EXTRA_ARGS
        return config
    }
}
