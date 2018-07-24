package com.redhat.ci.provisioner

import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost

interface Provisioner {
  ProvisionedHost provision(TargetHost target)
  void teardown(ProvisionedHost host)
}
