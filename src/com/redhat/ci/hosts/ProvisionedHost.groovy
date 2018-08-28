package com.redhat.ci.hosts

import com.redhat.ci.provider.Type
import com.redhat.ci.provisioner.Type

/**
 * A TargetHost that has undergone an attempt at provisioning a its associated state.
 */
class ProvisionedHost extends TargetHost {
    // The provider the host was provisioned from
    com.redhat.ci.provider.Type providerType = null

    // The provisioner the host was provisioned with
    com.redhat.ci.provisioner.Type provisionerType = null

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
}
