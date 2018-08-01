package com.redhat.ci.hosts

import org.junit.Test

/**
 * Tests if hosts can be created.
 */
class HostsTest {

    @Test
    void canCreateHost() {
        Host host = new Host()
        assert(host != null)
    }

    @Test
    void canCreateTargetHost() {
        TargetHost host = new TargetHost()
        assert(host != null)
    }

    @Test
    void canCreateProvisionedHost() {
        ProvisionedHost host = new ProvisionedHost()
        assert(host != null)
    }
}
