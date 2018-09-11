package com.redhat.ci.hosts

/**
 * A TargetHost that has undergone an attempt at provisioning a its associated state.
 */
class ProvisionedHost extends TargetHost {
    // The provider the host was provisioned from
    String providerType = null

    // The provisioner the host was provisioned with
    String provisionerType = null

    // Any error that occurred during provisioning
    String error = null

    // Name of the host as it will appear in Jenkins
    String displayName = null

    // Full path to the inventory file
    String inventoryPath = null

    // Whether provisioning initialization was successful
    Boolean initialized = false

    // Whether provisioning was successful
    Boolean provisioned = false

    // Whether connecting the host to Jenkins via JNLP was successful
    Boolean connectedToMaster = false

    // Whether installing Ansible on the host was successful
    Boolean ansibleInstalled = false

    // Whether installing credentials on the host was successful
    Boolean credentialsInstalled = false

    // Wheater installing rhpkg on the host was successful
    Boolean rhpkgInstalled = false

    ProvisionedHost() {
    }

    ProvisionedHost(TargetHost target) {
        super()
        this.id = target.id
        this.arch = target.arch
        this.hostname = target.hostname
        this.type = target.type
        this.typePriority = target.typePriority
        this.provider = target.provider
        this.providerPriority = target.providerPriority
        this.provisioner = target.provisioner
        this.provisioner = target.provisionerPriority
    }
}
