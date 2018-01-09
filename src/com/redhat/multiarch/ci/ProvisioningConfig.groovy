package com.redhat.multiarch.ci

class ProvisioningConfig {
    // Job group for Beaker provisioning.
    String jobgroup = 'multiarch-qe'
    // name of Openshift tenant to pull provisioning image from.
    String tenant = 'redhat-multiarch-qe'
    // docker url to pull provisioning image from.
    String dockerUrl = '172.30.1.1:5000'
    // name of provisioning image. Can include tag name.
    String provisioningImage = 'provisioner'
    // Provisioning repo url
    String provisioningRepoUrl = null
    // Provisioning repo ref
    String provisioningRepoRef = null
    // Provisioning workspace location (needed for Linchpin)
    // This can reference a relative path in the above repo
    // or it can reference a relative path that already exists
    // in the current directory
    String provisioningWorkspaceDir = 'workspace'
    // Kerberos principal for Beaker authentication.
    String krbPrincipal = 'jenkins/multiarch-qe-jenkins.rhev-ci-vms.eng.rdu2.redhat.com'
    // ID of Jenkins credential for keytab needed for Beaker authentication.
    String KEYTABCREDENTIALID = params.KEYTABCREDENTIALID ?: 'KEYTAB'
    // ID of Jenkins credential for SSH private key to will be
    // copied to provisioned resource.
    // *** This must be the same as what was added to Beaker ***
    String SSHPRIVKEYCREDENTIALID = params.SSHPRIVKEYCREDENTIALID ?: 'SSHPRIVKEY'
    // ID of Jenkins credential for SSH public key to will be
    // copied to provisioned resource
    // *** This must be the same as what was added to Beaker ***
    String SSHPUBKEYCREDENTIALID = params.SSHPUBKEYCREDENTIALID ?: 'SSHPUBKEY'
    // Whether the closure should be run on directly on the provisioned slave.
    Boolean runOnSlave = true
    // Whether Ansible should be installed on the provisioned slave.
    Boolean installAnsible = true
}