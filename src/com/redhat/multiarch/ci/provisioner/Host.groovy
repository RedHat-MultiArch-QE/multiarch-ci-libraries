package com.redhat.multiarch.ci.provisioner

class Host {
  // Architecture of the host
  String arch = null
  // Name of the host
  String hostName = null
  // Any error that occurred during provisioning
  String error = null
  // Name of the host as it will appear in Jenkins
  String name = null
  // Target of the Linchpin PinFile
  String target = null
  // Full path to the inventory file
  String inventory = null
  // Whether provisioning initialization was successful
  Boolean initialized = false
  // Whether provisioning was successful
  Boolean provisioned = false
  // Whether connecting the host to Jenkins via Cinch was successful
  Boolean connectedToMaster = false
  // Whether installing Ansible on the host was successful
  Boolean ansibleInstalled = false
  // Whether installing credentials on the host was successful
  Boolean credentialsInstalled = false
}
