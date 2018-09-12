package com.redhat.ci.provisioners

import org.junit.Test
import org.junit.Before
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost

/**
 * Tests methods belonging to KubeVirtProvisioner.
 */
class KubeVirtProvisionerTest {
    private KubeVirtProvisioner provisioner
    private PipelineTestScript script

    @Before
    void init() {
        provisioner = new KubeVirtProvisioner(script)
        script = new PipelineTestScript()
    }

    @Test
    void testProvision() {
        ProvisionedHost host = provisioner.provision(new TargetHost(), new ProvisioningConfig())
        assert(host == null)
    }

    @SuppressWarnings('JUnitTestMethodWithoutAssert')
    @Test
    void testTeardown() {
        provisioner.teardown(new ProvisionedHost(), new ProvisioningConfig())
    }
}
