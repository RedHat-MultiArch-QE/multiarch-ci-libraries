package com.redhat.ci.provisioners

import com.redhat.ci.provisioner.Provisioner
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost

/**
 * A base provisioner that defines shared utility methods to perform actions upon a provisioned host.
 */
abstract class AbstractProvisioner implements Provisioner {
    Script script = null
    String type = null
    protected List<String> supportedHostTypes = []
    protected List<String> supportedProviders = []
    protected Boolean available = false

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
    Boolean supportsHostType(String hostType) {
        supportedHostTypes.contains(hostType)
    }

    /**
     * Determines whether a provider is supported for provisioning.
     */
    @Override
    Boolean supportsProvider(String provider) {
        supportedProviders.contains(provider)
    }

    /**
     * Getter for availability status.
     */
    @Override
    Boolean getAvailable() {
        this.@available
    }
}
