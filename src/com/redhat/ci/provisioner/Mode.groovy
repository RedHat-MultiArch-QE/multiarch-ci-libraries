package com.redhat.ci.provisioner

/**
 * Enumeration of protocols used to interface with a provisioned host.
 *
 * We're not using the Java enum type since it's not supported directly by the groovy security sandbox.
 */
class Mode {
    public static final String JNLP = 'JNLP'
    public static final String SSH  = 'SSH'
}
