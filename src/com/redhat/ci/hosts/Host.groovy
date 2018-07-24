package com.redhat.ci.host

/**
 * Host primitives.
 */
class Host {
  // ID for the host
  String id = null
  // Hostname of the host
  String hostname = null
  // Host type specification (Baremetal, VM, container)
  Type type = null
  // Architecture of the target
  String arch = null
}
