package com.redhat.ci.provisioner

/**
 * Custom exception to throw when provisioning fails.
 */
class ProvisioningException extends Exception {
    ProvisioningException() {
    }

    ProvisioningException(String message) {
        super(message)
    }
}
