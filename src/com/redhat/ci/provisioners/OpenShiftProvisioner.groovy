package com.redhat.ci.provisioners

import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Type

/**
 * A stubbed provisioner template for OpenShift requests.
 */
class OpenShiftProvisioner extends AbstractProvisioner {

    OpenShiftProvisioner() {
        this(null)
    }

    OpenShiftProvisioner(Script script) {
        super(script)
        this.type = Type.OPENSHIFT
        this.supportedHostTypes = [com.redhat.ci.host.Type.CONTAINER]
        this.supportedProviders = [com.redhat.ci.provider.Type.OPENSHIFT]
    }

    @Override
    ProvisionedHost provision(TargetHost target, ProvisioningConfig config) {
        null
    }

    @Override
    void teardown(ProvisionedHost host, ProvisioningConfig config) {
    }
}
