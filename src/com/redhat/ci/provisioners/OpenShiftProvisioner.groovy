package com.redhat.ci.provisioners

import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Type

/**
 * A stubbed provisioner template for OpenShift requests.
 */
class OpenShiftProvisioner extends AbstractProvisioner {

    OpenShiftProvisioner(Script script, ProvisioningConfig config) {
        super(script, config)
        this.type = Type.OPENSHIFT
    }

    @Override
    ProvisionedHost provision(TargetHost target) {
        ProvisionedHost host = new ProvisionedHost()
        host
    }

    @Override
    void teardown(ProvisionedHost host) {
    }
}
