package com.redhat.ci.provisioners

import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Type

/**
 * A stubbed provisioner template for KubeVirt requests.
 */
class KubeVirtProvisioner extends AbstractProvisioner {
    KubeVirtProvisioner(Script script) {
        super(script)
        this.type = Type.KUBEVIRT
        this.supportedHostTypes = [com.redhat.ci.host.Type.VM]
        this.supportedProviders = [com.redhat.ci.provider.Type.KUBEVIRT]
    }

    @Override
    ProvisionedHost provision(TargetHost target, ProvisioningConfig config) {
        null
    }

    @Override
    void teardown(ProvisionedHost host, ProvisioningConfig config) {
    }
}
