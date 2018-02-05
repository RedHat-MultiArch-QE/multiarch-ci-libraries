package com.redhat.multiarch.ci.provisioner

class Host {
  String arch = null
  String hostName = null
  String error = null
  String name = null
  String target = null
  String inventory = null
  Boolean initialized = false
  Boolean provisioned = false
  Boolean connectedToMaster = false
  Boolean ansibleInstalled = false
}
