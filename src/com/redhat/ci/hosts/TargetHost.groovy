package com.redhat.ci.hosts

import com.redhat.ci.host.Type
import com.redhat.ci.provider.Type
import com.redhat.ci.provisioner.Type

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

    // Host type priority List
    List<com.redhat.ci.host.Type> typePriority = ProvisioningConfig.HOST_TYPE_PRIORITY_DEFAULT

    // Provide type priority list
    List<com.redhat.ci.provider.Type> providerPriority = ProvisioningConfig.PROVIDER_PRIORITY_DEFAULT

    // Provisioner type priority list
    List<com.redhat.ci.provisioner.Type> provisionerPriority = ProvisioningConfig.PROVISIONER_PRIORITY_DEFAULT
}
