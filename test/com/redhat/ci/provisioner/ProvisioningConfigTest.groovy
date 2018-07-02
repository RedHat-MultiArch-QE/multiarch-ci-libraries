package com.redhat.ci.provisioner

import org.junit.Test

public class ProvisioningConfigTest {

  @Test
  void should_support_legacy_api() {
    ProvisioningConfig config = new ProvisioningConfig()
    assert(config.mode == Mode.CINCH)
    config.runOnSlave = false
    assert(config.mode == Mode.SSH)
    config.runOnSlave = true
    assert(config.mode == Mode.CINCH)
  }
}
