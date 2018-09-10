package com.redhat.ci.provisioner

/**
 * Enumeration of provisioner types.
 *
 * We're not using the Java enum type since it's not supported directly by the groovy security sandbox.
 */
class Type extends String {
    public static final LINCHPIN = 'LINCHPIN'
    public static final KUBEVIRT = 'KUBEVIRT'
    public static final OPENSHIFT = 'OPENSHIFT'
}
