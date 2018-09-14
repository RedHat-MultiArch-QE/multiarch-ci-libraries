package com.redhat.ci.provisioner

/**
 * Custom exception to throw when provisioner is unavailable.
 */
class ProvisionerUnavailableException extends Exception {
    ProvisionerUnavailableException() {
    }

    ProvisionerUnavailableException(String message) {
        super(message)
    }
}
