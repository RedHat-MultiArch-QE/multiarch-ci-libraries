package com.redhat.ci.hosts

import org.junit.Test

/**
 * Tests if a TargetHost can be created.
 */
class TargetHostTest {

    @Test
    void canCreateTargetHost() {
        TargetHost host = new TargetHost()
        assert(host != null)
    }
}
