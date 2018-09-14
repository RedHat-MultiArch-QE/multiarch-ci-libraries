package com.redhat.ci.provisioner

import org.junit.Test
import com.redhat.ci.hosts.TargetHost

/**
 * Tests individual methods in the ProvisioningService.
 */
class ProvisioningServiceTest {
    private final Script script = new PipelineTestScript()
    private final ProvisioningService provSvc = new ProvisioningService()
    private final ProvisioningConfig config = new ProvisioningConfig()

    @Test
    void invalidProvisionerRequestThrowsException() {
        Boolean exceptionThrown = false
        TargetHost target = new TargetHost()
        target.provisioner = 'FAKE'
        try {
            provSvc.provision(target, config, script)
        } catch (ProvisionerUnavailableException e) {
            exceptionThrown = true
        }

        assert(exceptionThrown)
    }
}
