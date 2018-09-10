package com.redhat.ci.provisioner

/**
 * Enumeration of protocols used to interface with a provisioned host.
 */
enum Mode {
    JNLP, SSH

    @SuppressWarnings(['UnnecessaryConstructor', 'UnnecessaryPublicModifer'])
    public Mode() { }
}
