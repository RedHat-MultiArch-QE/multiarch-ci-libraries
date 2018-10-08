package com.redhat.ci.provisioner

/**
 * Enumeration of provisioner types.
 *
 * We're not using the Java enum type since it's not supported directly by the groovy security sandbox.
 */
class Type {
    public static final String LINCHPIN = 'LINCHPIN'
    public static final String KUBEVIRT = 'KUBEVIRT'
    public static final String OPENSHIFT = 'OPENSHIFT'
}
