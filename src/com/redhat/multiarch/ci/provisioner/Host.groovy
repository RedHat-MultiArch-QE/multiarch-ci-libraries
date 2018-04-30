package com.redhat.multiarch.ci.provisioner

class Host {
  // Architecture of the host
  String arch = null
  // Name of the host
  String hostName = null
  // Any error that occured during provisioning
  String error = null
  // Name of the host as it will appear in Jenkins
  String name = null
  // Target of the linchpin PinFile
  String target = null
  // Full path to the inventory file
  String inventory = null
  // Whether provisioning initializion was successful
  Boolean initialized = false
  // Whether provisioning was successful
  Boolean provisioned = false
  // Whether connecting the host to Jenkins via cinch was successful
  Boolean connectedToMaster = false
  // Whether installing ansible on the host was successful
  Boolean ansibleInstalled = false
}
