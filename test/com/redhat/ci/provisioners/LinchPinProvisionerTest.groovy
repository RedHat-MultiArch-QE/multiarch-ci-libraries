package com.redhat.ci.provisioners

import org.junit.Test
import org.junit.Before
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.Mode
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Provisioner

/**
 * Tests methods belonging to LinchPinProvisioner.
 */
class LinchPinProvisionerTest {
    private static final List<String> TEST_MODES = [Mode.SSH, Mode.JNLP]
    private static final String EXCEPTION_MESSAGE = 'An exception occured!'
    private LinchPinProvisioner provisioner
    private PipelineTestScript script

    @Before
    void init() {
        script = new PipelineTestScript()
        provisioner = new LinchPinProvisioner(script)
    }

    @Test
    void ensureUnavailableOnlyWhenScriptIsNull() {
        Provisioner nullScriptProvisioner = new LinchPinProvisioner()
        assert(!nullScriptProvisioner.available)
    }

    @Test
    void ensureAvailableWhenScriptIsNonNull() {
        assert(provisioner.available)
    }

    @Test
    void testMinimalProvision() {
        TEST_MODES.each {
            mode ->
            ProvisioningConfig config = new ProvisioningConfig()
            config.mode = mode
            config.installAnsible = false
            config.installCredentials = false
            config.installRhpkg = false
            config.provisioningRepoUrl = null

            ProvisionedHost host = provisioner.provision(new TargetHost(), config)
            assert(!host.error)
        }
    }

    @Test
    void testFullProvision() {
        TEST_MODES.each {
            mode ->
            ProvisioningConfig config = new ProvisioningConfig()
            config.mode = mode
            config.installAnsible = true
            config.installCredentials = true
            config.installRhpkg = true

            ProvisionedHost host = provisioner.provision(
                new TargetHost(bkrJobGroup:'maqe', bkrHostRequires:[tag:'hostname', value:'test', op:'=']),
                config)
            assert(!host.error)
        }
    }

    @Test
    void testFailedProvision() {
        Closure sh = {
            throw new TestException(EXCEPTION_MESSAGE)
        }

        script = new PipelineTestScript(sh:sh)
        provisioner = new LinchPinProvisioner(script)

        ProvisionedHost host = provisioner.provision(new TargetHost(), new ProvisioningConfig())
        assert(host.error == EXCEPTION_MESSAGE)
    }

    @Test
    void testProvisionWithInvalidLinchpinLatest() {
        Closure readJSON = {
            file ->
            [:]
        }

        script = new PipelineTestScript(readJSON:readJSON)
        provisioner = new LinchPinProvisioner(script)

        ProvisionedHost host = provisioner.provision(new TargetHost(), new ProvisioningConfig())

        assert(host.error)
    }

    @Test
    void testTeardown() {
        provisioner.teardown(new ProvisionedHost(initialized:true), new ProvisioningConfig())
        assert(!script.testLog)
    }

    @Test
    void testTeardownHostIsNullNoOp() {
        provisioner.teardown(null, new ProvisioningConfig())
        assert(script.currentBuild.result == 'SUCCESS')
        assert(!script.testLog)
    }

    @Test
    void testTeardownHostNoOp() {
        provisioner.teardown(new ProvisionedHost(initialized:false, error:EXCEPTION_MESSAGE),
                             new ProvisioningConfig())
        assert(script.currentBuild.result == 'FAILURE')
        assert(!script.testLog)
    }

    @Test
    void testFailedTeardown() {
        Closure sh = {
            throw new TestException(EXCEPTION_MESSAGE)
        }

        script = new PipelineTestScript(sh:sh)
        provisioner = new LinchPinProvisioner(script)

        ProvisioningConfig config = new ProvisioningConfig()
        config.mode = Mode.JNLP
        ProvisionedHost host = new ProvisionedHost(initialized:true, connectedToMaster:true)

        provisioner.teardown(host, config)

        assert(script.testLog.count("Exception: ${EXCEPTION_MESSAGE}") == 2)
    }
}
