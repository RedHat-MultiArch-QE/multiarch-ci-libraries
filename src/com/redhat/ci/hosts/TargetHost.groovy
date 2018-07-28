package com.redhat.ci.hosts

import com.redhat.ci.provisioner.Type

/**
 * A target host for provisioning.
 */
class TargetHost extends Host {
    // Provisioner enum specification
    Type provisionerType = Type.OPENSHIFT
    // Target of the PinFile if provisioner is type LINCHPIN
    String linchpinTarget = 'beaker-slave'
}
