package com.redhat.ci.hosts

/**
 * A target host for provisioning.
 */
class TargetHost extends Host {

    // Host type priority list
    List<String> typePriority = null

    // Selected provider type
    String provider = null

    // Provider type priority list
    List<String> providerPriority = null

    // Selected provisioner type
    String provisioner = null

    // Provisioner type priority list
    List<String> provisionerPriority = null
}
