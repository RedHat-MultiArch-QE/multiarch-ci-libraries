package com.redhat.ci.hosts

import com.redhat.ci.host.Type

/**
 * Host primitives.
 */
class Host {
    // ID for the host
    String id = UUID.randomUUID()

    // Hostname of the host
    String hostname = null

    // Architecture of the target
    String arch = null

    // Host type specification (Baremetal, VM, container)
    Type type = null
}
