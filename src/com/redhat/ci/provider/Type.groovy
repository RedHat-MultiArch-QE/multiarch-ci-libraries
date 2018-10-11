package com.redhat.ci.provider

/**
 * Enumeration of provider types.
 *
 * We're not using the Java enum type since it's not supported directly by the groovy security sandbox.
 */
class Type {
    public static final String AWS = 'AWS'
    public static final String BEAKER = 'BEAKER'
    public static final String DUFFY = 'DUFFY'
    public static final String KUBEVIRT = 'KUBEVIRT'
    public static final String OPENSHIFT = 'OPENSHIFT'
    public static final String OPENSTACK = 'OPENSTACK'
    public static final String UNKNOWN = 'UNKNOWN'
}
