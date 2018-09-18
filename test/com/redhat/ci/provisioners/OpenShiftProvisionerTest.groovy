package com.redhat.ci.provisioners

import org.junit.Test
import org.junit.Before
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Provisioner

/**
 * Tests methods belonging to the OpenShiftProvisioner.
 */
class OpenShiftProvisionerTest {
    private OpenShiftProvisioner provisioner
    private PipelineTestScript script

    @Before
    void init() {
        provisioner = new OpenShiftProvisioner(script)
        script = new PipelineTestScript()
    }

    @Test
    void ensureUnavailable() {
        Provisioner nullScriptProvisioner = new OpenShiftProvisioner()
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
        Boolean exceptionOccured = false
        try {
            provisioner.teardown(new ProvisionedHost(), new ProvisioningConfig())
        } catch (TestException e) {
            exceptionOccured = true
        }

        assert(!exceptionOccured)
    }
}
