package com.redhat.ci.provisioner

/**
 * Configuration needed to provision resources with a Provisioner.
 */
class ProvisioningConfig {
    private static final String KRB_PRINCIPAL_CREDENTIAL_ID_DEFAULT = 'redhat-multiarch-qe-krbprincipal'
    private static final String KEYTAB_CREDENTIAL_ID_DEFAULT = 'redhat-multiarch-qe-keytab'
    private static final String SSH_PRIV_KEY_CREDENTIAL_ID_DEFAULT = 'redhat-multiarch-qe-sshprivkey'
    private static final String SSH_PUB_KEY_CREDENTIAL_ID_DEFAULT = 'redhat-multiarch-qe-sshpubkey'
    private static final String JENKINS_SLAVE_CREDENTIAL_ID_DEFAULT = 'jenkins-slave-credentials'
    private static final String JENKINS_MASTER_URL_DEFAULT = ''
    private static final String JSWARM_EXTRA_ARGS_DEFAULT  = ''

    // Provisioner version
    String version = 'v1.1'
    // Jenkins kubernetes cloud name
    String cloudName = 'openshift'
    // Job group for Beaker provisioning.
    String jobgroup = ''
    // Host requirements to pass to linchpin
    List<HashMap> hostrequires = []
    // name of Openshift tenant to pull provisioning image from.
    String tenant = 'redhat-multiarch-qe'
    // docker url to pull provisioning image from.
    String dockerUrl = '172.30.1.1:5000'
    // name of provisioning image. Can include tag name.
    String provisioningImage = 'provisioner'
    // Provisioning repo url
    String provisioningRepoUrl = 'https://github.com/RedHat-MultiArch-QE/multiarch-ci-test-template'
    // Provisioning repo ref
    String provisioningRepoRef = this.version
    // Provisioning workspace location (needed for LinchPin)
    // This can reference a relative path in the above repo
    // or it can reference a relative path that already exists
    // in the current directory
    String provisioningWorkspaceDir = 'workspace'
    // ID of Jenkins credential for kerberos principal needed for Beaker authentication.
    String krbPrincipalCredentialId = KRB_PRINCIPAL_CREDENTIAL_ID_DEFAULT
    // ID of Jenkins credential for keytab needed for Beaker authentication.
    String keytabCredentialId = KEYTAB_CREDENTIAL_ID_DEFAULT
    // ID of the Jenkins credential for the krb conf needed for Beaker authentication.
    String krbConfCredentialId = 'redhat-multiarch-qe-krbconf'
    // ID of the Jenkins credential for the bkr conf needed for Beaker authentication.
    String bkrConfCredentialId = 'redhat-multiarch-qe-bkrconf'
    // ID of Jenkins credential for SSH private key to will be
    // copied to provisioned resource.
    // *** This must be the same as what was added to Beaker ***
    String sshPrivKeyCredentialId = SSH_PRIV_KEY_CREDENTIAL_ID_DEFAULT
    // ID of Jenkins credential for SSH public key to will be
    // copied to provisioned resource
    // *** This must be the same as what was added to Beaker ***
    String sshPubKeyCredentialId = SSH_PUB_KEY_CREDENTIAL_ID_DEFAULT
    // ID of the Jenkins credential for the username and password
    // used by JNLP to connect the provisioned host to the Jenkins master
    String jenkinsSlaveCredentialId = JENKINS_SLAVE_CREDENTIAL_ID_DEFAULT
    // URL of the Jenkins master that JNLP will use to connect the provisioned
    // host as a slave.
    String jenkinsMasterUrl = JENKINS_MASTER_URL_DEFAULT
    // Extra arguments passed to the jswarm call.
    // Allows for the connection to be tunneled in the case of an OpenShift hosted Jenkins.
    String jswarmExtraArgs = JSWARM_EXTRA_ARGS_DEFAULT
    // Determines whether connection to the provisioned host should be over JNLP or SSH.
    Boolean runOnSlave = true
    Mode mode = Mode.JNLP
    // Whether Ansible should be installed on the provisioned host.
    // This will only be respected if runOnSlave is also set to true,
    // since jobs that are run via ssh already have access to ansible in the
    // provisioning container.
    Boolean installAnsible = true
    // Whether the ssh keypair and kerberos keytab should be installed on the provisioned host
    // These are already installed on the provisioning container
    Boolean installCredentials = true
    // Whether rhpkg should be installed on the provisioned host
    // This is only needed for tests that will use it to install from pkgs.devel.redhat.com
    Boolean installRhpkg = false

    ProvisioningConfig(Map params = [:], Map env = [:]) {
        this.krbPrincipalCredentialId = params.KRBPRINCIPALCREDENTIALID ?: this.krbPrincipalCredentialId
        this.keytabCredentialId = params.KEYTABCREDENTIALID ?: this.keytabCredentialId
        this.sshPrivKeyCredentialId = params.SSHPRIVKEYCREDENTIALID ?: this.sshPrivKeyCredentialId
        this.sshPubKeyCredentialId = params.SSHPUBKEYCREDENTIALID ?: this.sshPubKeyCredentialId
        this.jenkinsSlaveCredentialId = params.JENKINSSLAVECREDENTIALID ?: this.jenkinsSlaveCredentialId
        this.jenkinsMasterUrl = env.JENKINS_MASTER_URL ?: this.jenkinsMasterUrl
        this.jswarmExtraArgs = env.JSWARM_EXTRA_ARGS ?: this.jswarmExtraArgs
    }

    void setRunOnSlave(Boolean runOnSlave) {
        this.runOnSlave = runOnSlave
        this.mode = runOnSlave ? Mode.JNLP : Mode.SSH
    }

    void setMode(Mode mode) {
        this.mode = mode
        this.runOnSlave = (mode == Mode.JNLP)
    }
}