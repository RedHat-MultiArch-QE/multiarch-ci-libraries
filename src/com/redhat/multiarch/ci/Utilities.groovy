package com.redhat.multiarch.ci

class Utilities {
  static def createProvisioningConfig(script) {
    def config = new ProvisioningConfig()
    config.KEYTABCREDENTIALID = script.params.KEYTABCREDENTIALID ?: config.KEYTABCREDENTIALID
    config.SSHPRIVKEYCREDENTIALID = script.params.SSHPRIVKEYCREDENTIALID ?: config.SSHPRIVKEYCREDENTIALID
    config.SSHPUBKEYCREDENTIALID = script.params.SSHPUBKEYCREDENTIALID ?: config.SSHPUBKEYCREDENTIALID
    config.JENKINSSLAVECREDENTIALID = script.params.JENKINSSLAVECREDENTIALID ?: config.JENKINSSLAVECREDENTIALID
    config.JENKINS_MASTER_URL = "{script.env.JENKINS_MASTER_URL}" ?: config.JENKINS_MASTER_URL
    config.JSWARM_EXTRA_ARGS = "${script.env.JSWARM_EXTRA_ARGS}" ?: config.JSWARM_EXTRA_ARGS
    return config
  }
}

