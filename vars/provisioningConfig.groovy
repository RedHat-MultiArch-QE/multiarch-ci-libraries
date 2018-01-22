import com.redhat.multiarch.ci.ProvisioningConfig

class provisioningConfig {

    def create(params, env) {
        def config = new ProvisioningConfig()
        config.KEYTABCREDENTIALID = params.KEYTABCREDENTIALID ?: config.KEYTABCREDENTIALID
        config.SSHPRIVKEYCREDENTIALID = params.SSHPRIVKEYCREDENTIALID ?: config.SSHPRIVKEYCREDENTIALID
        config.SSHPUBKEYCREDENTIALID = params.SSHPUBKEYCREDENTIALID ?: config.SSHPUBKEYCREDENTIALID
        config.JENKINSSLAVECREDENTIALID = params.JENKINSSLAVECREDENTIALID ?: config.JENKINSSLAVECREDENTIALID
        config.JENKINS_MASTER_URL = env.JENKINS_MASTER_URL ?: config.JENKINS_MASTER_URL
        config.JSWARM_EXTRA_ARGS = env.JSWARM_EXTRA_ARGS ?: config.JSWARM_EXTRA_ARGS
        return config
    }
}
