package com.redhat.ci.host

/**
 * Enumeration of host types.
 *
 * We're not using the Java enum type since it's not supported directly by the groovy security sandbox.
 */
class Type {
    public static final String BAREMETAL = 'BAREMETAL'
    public static final String VM = 'VM'
    public static final String CONTAINER = 'CONTAINER'
}
