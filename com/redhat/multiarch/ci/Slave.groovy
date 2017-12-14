package com.redhat.multiarch.ci

class Slave {
  def String name = null
  def String hostName = null
  def Boolean provisioned = false
  def Boolean connectedToMaster = false
  def Boolean ansibleInstalled = false
  def String error = null
}
