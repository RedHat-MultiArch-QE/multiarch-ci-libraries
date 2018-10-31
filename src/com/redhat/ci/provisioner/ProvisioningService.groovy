package com.redhat.ci.provisioner

import com.redhat.ci.provisioners.LinchPinProvisioner
import com.redhat.ci.provisioners.KubeVirtProvisioner
import com.redhat.ci.provisioners.OpenShiftProvisioner
import com.redhat.ci.provisioners.NoOpProvisioner
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.host.Type

/**
 * Utilities for smart provisioning.
 * Attempts to minimize resource footprint.
 */
@SuppressWarnings('AbcMetric')
class ProvisioningService {
    public static final String UNAVAILABLE = 'No available provisioner could provision target.'

    @SuppressWarnings(['NestedForLoop', 'MethodSize'])
    ProvisionedHost provision(TargetHost target, ProvisioningConfig config, Script script) {
        Provisioner provisioner = null
        ProvisionedHost host = null

        // Users can override the priority list by manually entering their desired provisioner type
        if (target.provisioner) {
            target.provisionerPriority = [target.provisioner]

            // We explicitly set the host type and provider to UNKNOWN to prevent propagation of inaccurate information
            // since the NoOpProvisioner doesn't care what kind of host it's provisioning
            if (target.provisioner == com.redhat.ci.provisioner.Type.NOOP) {
                target.provider = target.provider ?: com.redhat.ci.provider.Type.UNKNOWN
                target.type = target.type ?: Type.UNKNOWN
            }
        }

        // Users can override the priority list by manually entering their desired host type
        if (target.type) {
            target.typePriority = [target.type]
        }

        // Users can override the priority list by manually entering their desired provider type
        if (target.provider) {
            target.providerPriority = [target.provider]
        }

        // Ensure there is a default set for the host type priority
        target.typePriority = target.typePriority ?: config.hostTypePriority

        // Ensure there is a default set for the provisioner priority
        if (target.provisionerPriority == null) {
            target.provisionerPriority = config.provisionerPriority
        }

        // Ensure there is a default set for the provider priority
        target.providerPriority = target.providerPriority ?: config.providerPriority

        // Loop through each provisioner type by priority
        for (provisionerType in target.provisionerPriority) {
            // Verify that there is an available provisioner of this type
            provisioner = getProvisioner(provisionerType, script)

            // Check if provisioner is available
            if (!provisioner.available) {
                script.echo("Provisioning host with ${provisionerType} provisioner is not possible. " +
                            "Provisioner ${provisionerType} not available.")
                continue
            }

            // Loop through each host type by priority
            for (hostType in target.typePriority) {
                // Check if provisioner supports host type
                if (!provisioner.supportsHostType(hostType)) {
                    script.echo("Provisioning ${hostType} host " +
                                "with ${provisionerType} provisioner is not supported.")
                    continue
                }

                // Now that we've found a suitable provisioner, let's loop through providers
                for (providerType in target.providerPriority) {
                    // Verify that the selected provisioner supports the selected provider
                    if (!provisioner.supportsProvider(providerType)) {
                        script.echo("Provisioning ${hostType} host " +
                                    "with ${provisionerType} provisioner " +
                                    "and ${providerType} provider is not supported.")
                        continue
                    }

                    // Attempt to provision with the selected provisioner and provider pair
                    target.provisioner = provisionerType
                    target.provider = providerType
                    target.type = hostType

                    script.echo("Attempting to provision ${hostType} host " +
                                "with ${provisionerType} provisioner " +
                                "and ${providerType} provider.")

                    try {
                        host = provisioner.provision(target, config)
                    } catch (e) {
                        host = new ProvisionedHost(target)
                        host.error = e.message
                    }

                    if (host.error) {
                        // Provisioning failed, so try next provider
                        script.echo("Provisioning ${hostType} host " +
                                    "with ${provisionerType} provisioner " +
                                    "and ${providerType} provider failed.")
                        script.echo("Exception: ${host.error}")
                        continue
                    }

                    return host
                }
            }
        }

        // If we haven't returned from the function yet, we are out of available
        // hostType, provisioner, and provider combinations.
        throw new ProvisioningException(UNAVAILABLE)
    }

    void teardown(ProvisionedHost host, ProvisioningConfig config, Script script) {
        Provisioner provisioner = getProvisioner(host.provisioner, script)
        provisioner.teardown(host, config)
    }

    protected Provisioner getProvisioner(String provisioner, Script script) {
        switch (provisioner) {
            case com.redhat.ci.provisioner.Type.LINCHPIN:
                return new LinchPinProvisioner(script)
            case com.redhat.ci.provisioner.Type.OPENSHIFT:
                return new OpenShiftProvisioner(script)
            case com.redhat.ci.provisioner.Type.KUBEVIRT:
                return new KubeVirtProvisioner(script)
            case com.redhat.ci.provisioner.Type.NOOP:
                return new NoOpProvisioner(script)
            default:
                script.echo("Unrecognized provisioner:${provisioner}")
                throw new ProvisioningException(UNAVAILABLE)
        }
    }
}
