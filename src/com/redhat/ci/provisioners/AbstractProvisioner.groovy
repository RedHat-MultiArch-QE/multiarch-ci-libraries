package com.redhat.ci.provisioners

import com.redhat.ci.provisioner.Provisioner
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.Type
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost

/**
 * A base provisioner that defines shared utility methods to perform actions upon a provisioned host.
 */
abstract class AbstractProvisioner implements Provisioner {
    Script script = null
    Boolean available = false
    protected List<com.redhat.ci.host.Type> supportedHostTypes = []
    protected List<com.redhat.ci.provider.Type> supportedProviders = []
    Type type = null

    protected AbstractProvisioner(Script script) {
        this.script = script
    }

    /**
     * Attempts to provision the TargetHost.
     *
     * @param target TargetHost Specifies parameters for the host to provision.
     */
    abstract ProvisionedHost provision(TargetHost target, ProvisioningConfig config)

    /**
     * Attempts a teardown of the ProvisionedHost.
     *
     * @param host ProvisionedHost Host to be torn down.
     */
    abstract void teardown(ProvisionedHost host, ProvisioningConfig config)

    /**
     * Determines whether a host type is supported for provisioning.
     */
    @Override
    Boolean supportsHostType(com.redhat.ci.host.Type hostType) {
        supportedHostTypes.contains(hostType)
    }

    /**
     * Determines whether a provider is supported for provisioning.
     */
    @Override
    Boolean supportsProvider(com.redhat.ci.provider.Type provider) {
        supportedProviders.contains(provider)
    }
}
