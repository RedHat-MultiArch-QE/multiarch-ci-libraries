package com.redhat.ci.provisioner

import org.junit.Test

/**
 * Exhaustive coverage of provisioner types.
 */
class TypeTest {

    @Test
    void supportsLinchPin() {
        assert(Type.LINCHPIN == 'LINCHPIN')
    }

    @Test
    void supportsKubeVirt() {
        assert(Type.KUBEVIRT == 'KUBEVIRT')
    }

    @Test
    void supportsContainer() {
        assert(Type.OPENSHIFT == 'OPENSHIFT')
    }
}
