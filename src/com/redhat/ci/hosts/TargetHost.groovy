package com.redhat.ci.hosts

import com.redhat.ci.host.Type
import com.redhat.ci.provider.Type
import com.redhat.ci.provisioner.Type

/**
 * A target host for provisioning.
 */
class TargetHost extends Host {

    // Host type priority list
    List<com.redhat.ci.host.Type> typePriority = null

    // Selected provider type
    com.redhat.ci.provider.Type provider = null

    // Provider type priority list
    List<com.redhat.ci.provider.Type> providerPriority = null

    // Selected provisioner type
    Type provisioner = null

    // Provisioner type priority list
    List<com.redhat.ci.provisioner.Type> provisionerPriority = null
}
