package com.redhat.ci.provisioner

/**
 * Configuration needed to provision resources with a Provisioner.
 */
class ProvisioningConfig {
    private static final String RELEASE_VERSION = 'v1.3.0'
    private static final String KRB_PRINCIPAL_CREDENTIAL_ID_DEFAULT = 'redhat-multiarch-qe-krbprincipal'
    private static final String KEYTAB_CREDENTIAL_ID_DEFAULT = 'redhat-multiarch-qe-keytab'
    private static final String SSH_PRIV_KEY_CREDENTIAL_ID_DEFAULT = 'redhat-multiarch-qe-sshprivkey'
    private static final String SSH_PUB_KEY_CREDENTIAL_ID_DEFAULT = 'redhat-multiarch-qe-sshpubkey'
    private static final String JENKINS_SLAVE_CREDENTIAL_ID_DEFAULT = 'jenkins-slave-credentials'
    private static final String JENKINS_MASTER_URL_DEFAULT = ''
    private static final String JSWARM_EXTRA_ARGS_DEFAULT  = ''
    private static final String PROVISIONING_REPO_URL_DEFAULT =
      'https://github.com/redhat-multiarch-qe/multiarch-ci-libraries'
    private static final String PROVISIONING_REPO_REF_DEFAULT = RELEASE_VERSION

    // Provisioner version
    String version = RELEASE_VERSION

    // Jenkins kubernetes cloud name
    String cloudName = 'openshift'

    // Job group for Beaker provisioning. Kerberos service tenant must be in group if specified.
    String jobgroup = ''

    // Host requirements to pass to linchpin
    List<HashMap> hostrequires = []

    // Name of Openshift tenant to pull provisioning image from.
    String tenant = 'redhat-multiarch-qe'

    // Docker url to pull provisioning image from.
    String dockerUrl = '172.30.1.1:5000'

    // Name of provisioning image. Can include tag name.
    String provisioningImage = 'provisioner'

    // Provisioning repo url
    String provisioningRepoUrl = PROVISIONING_REPO_URL_DEFAULT

    // Provisioning repo ref
    String provisioningRepoRef = PROVISIONING_REPO_REF_DEFAULT

    // Provisioning workspace location (needed for LinchPin)
    // This can reference a relative path in the above repo
    // or it can reference a relative path that already exists
    // in the current directory
    String provisioningWorkspaceDir = 'workspace'

    // The provisioning priority for host types (e.g. containers vs VMs vs bare metal)
    List<String> hostTypePriority = []

    // The provisioning priority for the provider type (e.g. OpenShift vs. OpenStack vs. Beaker)
    List<String> providerPriority = []

    // The provisioning priority for the underlying provisioner (e.g. OpenShift API vs LinchPin)
    List<String> provisionerPriority = []

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

    // Version of the jswarm executable
    String jswarmVersion = '3.17'

    // Extra arguments passed to the jswarm call.
    // Allows for the connection to be tunneled in the case of an OpenShift hosted Jenkins.
    String jswarmExtraArgs = JSWARM_EXTRA_ARGS_DEFAULT

    // Determines whether connection to the provisioned host should be over JNLP or SSH.
    String mode = Mode.SSH

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

    // Whether the job should teardown resources when complete
    Boolean teardown = true

    @SuppressWarnings('AbcMetric')
    ProvisioningConfig(Map params = [:], Map env = [:]) {
        params = params ?: [:]
        env = env ?: [:]
        this.krbPrincipalCredentialId = params.KRBPRINCIPALCREDENTIALID ?: this.krbPrincipalCredentialId
        this.keytabCredentialId = params.KEYTABCREDENTIALID ?: this.keytabCredentialId
        this.sshPrivKeyCredentialId = params.SSHPRIVKEYCREDENTIALID ?: this.sshPrivKeyCredentialId
        this.sshPubKeyCredentialId = params.SSHPUBKEYCREDENTIALID ?: this.sshPubKeyCredentialId
        this.jenkinsSlaveCredentialId = params.JENKINSSLAVECREDENTIALID ?: this.jenkinsSlaveCredentialId
        this.jenkinsMasterUrl = env.JENKINS_MASTER_URL ?: this.jenkinsMasterUrl
        this.jswarmExtraArgs = env.JSWARM_EXTRA_ARGS ?: this.jswarmExtraArgs
        this.provisioningRepoUrl = params.LIBRARIES_REPO ?: this.provisioningRepoUrl
        this.provisioningRepoRef = params.LIBRARIES_REF ?: this.provisioningRepoRef

        hostTypePriority = [
            com.redhat.ci.host.Type.CONTAINER,
            com.redhat.ci.host.Type.VM,
            com.redhat.ci.host.Type.BAREMETAL,
        ]

        providerPriority = [
            com.redhat.ci.provider.Type.OPENSHIFT,
            com.redhat.ci.provider.Type.KUBEVIRT,
            com.redhat.ci.provider.Type.OPENSTACK,
            com.redhat.ci.provider.Type.BEAKER,
            com.redhat.ci.provider.Type.AWS,
            com.redhat.ci.provider.Type.DUFFY,
        ]

        provisionerPriority = [
            Type.OPENSHIFT,
            Type.KUBEVIRT,
            Type.LINCHPIN,
        ]
    }

    void setRunOnSlave(Boolean runOnSlave) {
        this.mode = runOnSlave ? Mode.JNLP : Mode.SSH
    }

    Boolean getRunOnSlave() {
        this.mode == Mode.JNLP
    }
}
