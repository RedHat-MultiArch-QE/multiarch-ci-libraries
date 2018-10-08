package com.redhat.ci.provider

import org.junit.Test

/**
 * Exhaustive coverage of provider types.
 */
class TypeTest {
    @Test
    void supportsAWS() {
        assert(Type.AWS == 'AWS')
    }

    @Test
    void supportsBeaker() {
        assert(Type.BEAKER == 'BEAKER')
    }

    @Test
    void supportsDuffy() {
        assert(Type.DUFFY == 'DUFFY')
    }

    @Test
    void supportsKubeVirt() {
        assert(Type.KUBEVIRT == 'KUBEVIRT')
    }

    @Test
    void supportsOpenShift() {
        assert(Type.OPENSHIFT == 'OPENSHIFT')
    }

    @Test
    void supportsOpenStack() {
        assert(Type.OPENSTACK == 'OPENSTACK')
    }
}
