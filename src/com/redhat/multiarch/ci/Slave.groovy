package com.redhat.multiarch.ci

class Slave {
  String arch = null
  String hostName = null
  String error = null
  String name = null
  String target = null
  String inventory = null
  Boolean provisioned = false
  Boolean connectedToMaster = false
  Boolean ansibleInstalled = false
}
