package com.redhat.ci.host

import org.junit.Test

/**
 * Exhaustive coverage of host types.
 */
class TypeTest {

    @Test
    void supportsBaremetal() {
        assert(Type.BAREMETAL == 'BAREMETAL')
    }

    @Test
    void supportsVM() {
        assert(Type.VM == 'VM')
    }

    @Test
    void supportsContainer() {
        assert(Type.CONTAINER == 'CONTAINER')
    }
}
