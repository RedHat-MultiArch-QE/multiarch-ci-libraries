package com.redhat.ci.hosts

import org.junit.Test

public class HostsTest {

  @Test
  void can_create_host() {
    Host host = new Host()
  }

  @Test
  void can_create_target_host() {
    TargetHost host = new TargetHost()
  }

  @Test
  void can_create_provisioned_host() {
    ProvisionedHost host = new ProvisionedHost()
  }
}
