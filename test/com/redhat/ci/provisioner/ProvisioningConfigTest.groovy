package com.redhat.ci.provisioner

import org.junit.Test

public class ProvisioningConfigTest {

  @Test
  void should_support_legacy_api() {
    ProvisioningConfig config = new ProvisioningConfig()
    assert(config.mode == Mode.JNLP)
    config.runOnSlave = false
    assert(config.mode == Mode.SSH)
    config.runOnSlave = true
    assert(config.mode == Mode.JNLP)

    config = new ProvisioningConfig()
    assert(config.runOnSlave == true)
    config.mode = Mode.SSH
    assert(config.runOnSlave == false)
    config.mode = Mode.JNLP
    assert(config.runOnSlave == true)
  }
}
