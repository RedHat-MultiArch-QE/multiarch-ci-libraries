package com.redhat.ci.hosts

import org.junit.Test

/**
 * Tests if hosts can be created.
 */
class HostsTest {

    @Test
    void can_create_host() {
        Host host = new Host()
        assert(host != null)
    }

    @Test
    void can_create_target_host() {
        TargetHost host = new TargetHost()
        assert(host != null)
    }

    @Test
    void can_create_provisioned_host() {
        ProvisionedHost host = new ProvisionedHost()
        assert(host != null)
    }
}
