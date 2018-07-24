package com.redhat.ci.hosts

class ProvisionedHost extends TargetHost {
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
