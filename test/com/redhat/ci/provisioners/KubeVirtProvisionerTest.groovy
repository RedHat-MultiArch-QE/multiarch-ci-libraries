package com.redhat.ci.provisioners

import org.junit.Test
import org.junit.Before
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Provisioner

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
    void ensureUnavailable() {
        Provisioner nullScriptProvisioner = new KubeVirtProvisioner()
        assert(!nullScriptProvisioner.available)
        assert(!provisioner.available)
    }

    @Test
    void testProvision() {
        ProvisionedHost host = provisioner.provision(new TargetHost(), new ProvisioningConfig())
        assert(host == null)
    }

    @Test
    void testTeardown() {
        provisioner.teardown(new ProvisionedHost(), new ProvisioningConfig())
        assert(!script.testLog)
    }
}
