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

    // Flag that allows linchpinTarget to be disabled for custom PinFiles
    Boolean linchpinTargetEnabled = true

    // Target of the linchpin PinFile (if linchpinTargetEnabled flag is set to true)
    String linchpinTarget = null

    // Beaker hostrequires
    // Overrides ProvisioningConfig's hostrequires
    List<Map> bkrHostRequires = null

    // Beaker keyvalue
    List<String> bkrKeyValue = null

    // Beaker jobgroup
    // Overrides ProvisioningConfig's jobgroup
    String bkrJobGroup = null

    // Beaker ks_meta
    String bkrKsMeta = null

    // Beaker kernel_options
    String bkrKernelOptions = null

    // Beaker kernel_options_post
    String bkrKernelOptionsPost = null

    // Beaker installation method
    String bkrMethod = null

    // Reservation duration
    Integer reserveDuration = null

    // String of parameters to pass to script tests
    String scriptParams = null

    // Remote user to connect with in SSH mode
    String remoteUser = 'root'

    // Inventory file variables
    Map inventoryVars = [:]
}
