package com.redhat.ci.provisioner

import org.junit.Test

/**
 * Exhaustive coverage of host types.
 */
class TypeTest {

    @Test
    void shouldSupportLinchPin() {
        assert(Type.LINCHPIN == 'LINCHPIN')
    }

    @Test
    void shouldSupportKubeVirt() {
        assert(Type.KUBEVIRT == 'KUBEVIRT')
    }

    @Test
    void shouldSupportContainer() {
        assert(Type.OPENSHIFT == 'OPENSHIFT')
    }
}
