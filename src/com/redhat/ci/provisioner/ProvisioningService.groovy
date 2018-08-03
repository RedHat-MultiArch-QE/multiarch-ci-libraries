package com.redhat.ci.provisioner

import com.redhat.ci.provisioners.LinchPinProvisioner
import com.redhat.ci.provisioners.KubeVirtProvisioner
import com.redhat.ci.provisioners.OpenShiftProvisioner
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.host.Type

/**
 * Utilities for smart provisioning.
 * Attempts to minimize resource footprint.
 */
class ProvisioningService {
    private static final String UNAVAILABLE = 'No available provisioner to provision target host.'

    Script script
    ProvisioningConfig config
    LinchPinProvisioner linchPin
    KubeVirtProvisioner kubeVirt
    OpenShiftProvisioner openShift

    ProvisioningService(Script script, ProvisioningConfig config) {
        this.script = script
        this.config = config
        this.linchPin  = new LinchPinProvisioner(script,  config)
        this.kubeVirt  = new KubeVirtProvisioner(script,  config)
        this.openShift = new OpenShiftProvisioner(script, config)
    }

    Provisioner getProvisioner(TargetHost host) {
        switch (host.type) {
            case Type.CONTAINER:
                if (openShift.available) {
                    return openShift
                }
            case Type.VM:
                if (kubeVirt.available) {
                    return kubeVirt
                }
                if (linchPin.available) {
                    return linchPin
                }
            case Type.BAREMETAL:
                if (linchPin.available) {
                    return linchPin
                }
            default:
                throw new ServiceUnavailableException(UNAVAILABLE)
        }
    }

    class ServiceUnavailableException extends RuntimeException {
        ServiceUnavailableException(String message) {
            super(message)
        }
    }
}
