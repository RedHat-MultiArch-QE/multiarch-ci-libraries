package com.redhat.ci.host

import com.redhat.ci.provisioner.Type

/**
 * A target host for provisioning.
 */
class TargetHost extends Host {
  // Provisioner enum specification
  com.redhat.ci.provisioner.Type provisionerType = null
  // Target of the PinFile if provisioner is type LINCHPIN
  String linchpinTarget = null
}
