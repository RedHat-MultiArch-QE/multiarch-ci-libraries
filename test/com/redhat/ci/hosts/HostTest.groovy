package com.redhat.ci.hosts

import org.junit.Test

/**
 * Tests if a Host can be created.
 */
class HostTest {

    @Test
    void canCreateHost() {
        Host host = new Host()
        assert(host != null)
    }
}
