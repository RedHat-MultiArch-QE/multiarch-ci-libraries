package com.redhat.multiarch.ci

class ProvisioningConfig {
    // Job group for Beaker provisioning.
    String jobgroup = null
    // name of Openshift tenant to pull provisioning image from.
    String tenant = null
    // docker url to pull provisioning image from.
    String dockerUrl = null
    // name of provisioning image. Can include tag name.
    String provisioningImage = null
    // Provisioning repo url
    String provisioningRepoUrl = null
    // Provisioning repo ref
    String provisioningRepoRef = null
    // Kerberos principal for Beaker authentication.
    String krbPrincipal = null
    // ID of Jenkins credential for keytab needed for Beaker authentication.
    String KEYTABCREDENTIALID = null
    // Whether the closure should be run on directly on the provisioned slave.
    Boolean runOnSlave = true
    // Whether Ansible should be installed on the provisioned slave.
    Boolean installAnsible = true
}