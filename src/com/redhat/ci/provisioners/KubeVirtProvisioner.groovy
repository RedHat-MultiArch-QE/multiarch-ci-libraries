package com.redhat.ci.provisioners

import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Type

/**
 * A stubbed provisioner template for KubeVirt requests.
 */
class KubeVirtProvisioner extends AbstractProvisioner {

    KubeVirtProvisioner(Script script, ProvisioningConfig config) {
        super(script, config)
        this.type = Type.KUBEVIRT
    }

    @Override
    ProvisionedHost provision(TargetHost target) {
        null
    }

    @Override
    void teardown(ProvisionedHost host) {
    }
}
