package com.redhat.ci.provisioner

import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost

/**
 * Defines the interface to a generic provisioner.
 */
interface Provisioner {
    Boolean getAvailable()
    ProvisionedHost provision(TargetHost target, ProvisioningConfig config)
    void teardown(ProvisionedHost host, ProvisioningConfig config)
    Boolean supportsHostType(String hostType)
    List<String> filterSupportedHostTypes(List<String> hostTypes)
    Boolean supportsProvider(String providerType)
}
