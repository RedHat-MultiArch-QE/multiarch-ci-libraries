package com.redhat.ci.provider

import org.junit.Test

/**
 * Exhaustive coverage of provider types.
 */
class TypeTest {
    @Test
    void shouldSupportAWS() {
        assert(Type.AWS == 'AWS')
    }

    @Test
    void shouldSupportBeaker() {
        assert(Type.BEAKER == 'BEAKER')
    }

    @Test
    void shouldSupportDuffy() {
        assert(Type.DUFFY == 'DUFFY')
    }

    @Test
    void shouldSupportKubeVirt() {
        assert(Type.KUBEVIRT == 'KUBEVIRT')
    }

    @Test
    void shouldSupportOpenShift() {
        assert(Type.OPENSHIFT == 'OPENSHIFT')
    }

    @Test
    void shouldSupportOpenStack() {
        assert(Type.OPENSTACK == 'OPENSTACK')
    }
}
