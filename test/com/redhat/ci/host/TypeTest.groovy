package com.redhat.ci.host

import org.junit.Test

/**
 * Exhaustive coverage of host types.
 */
class TypeTest {

    @Test
    void shouldSupportBaremetal() {
        assert(Type.BAREMETAL == 'BAREMETAL')
    }

    @Test
    void shouldSupportVM() {
        assert(Type.VM == 'VM')
    }

    @Test
    void shouldSupportContainer() {
        assert(Type.CONTAINER == 'CONTAINER')
    }
}
