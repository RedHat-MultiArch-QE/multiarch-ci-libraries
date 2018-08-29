package com.redhat.ci.hosts

import com.redhat.ci.host.Type
import com.redhat.ci.provider.Type
import com.redhat.ci.provisioner.Type
import com.redhat.ci.provisioner.ProvisioningConfig

/**
 * A target host for provisioning.
 */
class TargetHost extends Host {

    TargetHost() {
        super()
    }

    TargetHost(ProvisioningConfig config) {
        super()
        this.typePriority = config.hostTypePriority
        this.provisionerPriority = config.provisionerPriority
        this.providerPriority = config.providerPriority
    }

    // Host type priority list
    List<com.redhat.ci.host.Type> typePriority = ProvisioningConfig.HOST_TYPE_PRIORITY_DEFAULT

    // Selected provider type
    com.redhat.ci.provider.Type provider = null

    // Provider type priority list
    List<com.redhat.ci.provider.Type> providerPriority = ProvisioningConfig.PROVIDER_PRIORITY_DEFAULT

    // Selected provisioner type
    Type provisioner = null

    // Provisioner type priority list
    List<com.redhat.ci.provisioner.Type> provisionerPriority = ProvisioningConfig.PROVISIONER_PRIORITY_DEFAULT
}
