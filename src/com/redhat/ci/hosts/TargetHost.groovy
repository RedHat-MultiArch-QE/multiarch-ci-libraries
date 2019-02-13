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

    // Beaker hostrequires
    // Overrides ProvisioningConfig's hostrequires
    List<Map> bkrHostRequires = null

    // Beaker jobgroup
    // Overrides ProvisioningConfig's jobgroup
    String bkrJobGroup = null

    // Beaker ks_meta
    String bkrKsMeta = null

    // Beaker installation method
    String bkrMethod = null

    // Reservation duration
    Integer reserveDuration = null

    // String of parameters to pass to script tests
    String scriptParams = null

    // Remote user to connect with in SSH mode
    String remoteUser = 'root'
}
