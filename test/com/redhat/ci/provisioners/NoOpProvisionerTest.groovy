package com.redhat.ci.provisioners

import org.junit.Test
import org.junit.Before
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.Mode
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.Provisioner
import com.redhat.ci.provisioner.ProvisioningException

/**
 * Tests methods belonging to NoOpProvisioner.
 */
class NoOpProvisionerTest {
    private static final List<String> TEST_MODES = [Mode.SSH, Mode.JNLP]
    private static final String EXCEPTION_MESSAGE = 'An exception occured!'
    private static final String TEST_HOSTNAME = 'test-host.redhat.com'
    private static final String X86_64 = 'x86_64'
    private NoOpProvisioner provisioner
    private PipelineTestScript script

    @Before
    void init() {
        script = new PipelineTestScript()
        provisioner = new NoOpProvisioner(script)
    }

    @Test
    void ensureUnavailableOnlyWhenScriptIsNull() {
        Provisioner nullScriptProvisioner = new NoOpProvisioner()
        assert(!nullScriptProvisioner.available)
    }

    @Test
    void ensureAvailableWhenScriptIsNonNull() {
        assert(provisioner.available)
    }

    @Test
    void provisioningFailsWhenMissingArch() {
        ProvisioningConfig config = new ProvisioningConfig()
        ProvisionedHost host = provisioner.provision(new TargetHost(hostname:TEST_HOSTNAME), config)
        assert(host.error)
    }

    @Test
    void provisioningFailsWhenMissingHostname() {
        ProvisioningConfig config = new ProvisioningConfig()
        ProvisionedHost host = provisioner.provision(new TargetHost(arch:X86_64), config)
        assert(host.error)
    }

    @Test
    void provisioningFailsWhenMissingInventoryPath() {
        ProvisioningConfig config = new ProvisioningConfig()
        Closure noWrite = {
            Map map ->
            throw new ProvisioningException('Error writing file')
        }

        script = new PipelineTestScript(writeFile:noWrite)
        provisioner = new NoOpProvisioner(script)

        ProvisionedHost host = provisioner.provision(new TargetHost(hostname:TEST_HOSTNAME, arch:X86_64), config)
        assert(host.error)
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

            ProvisionedHost host = provisioner.provision(new TargetHost(hostname:TEST_HOSTNAME, arch:X86_64), config)
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
                new TargetHost(
                    hostname:TEST_HOSTNAME,
                    arch:X86_64,
                    inventoryVars:[ ansible_python_interpreter:'/usr/libexec/platform-python' ]
                ), config)
            assert(!host.error)
        }
    }

    @Test
    void testFailedProvision() {
        Closure sh = {
            throw new TestException(EXCEPTION_MESSAGE)
        }

        script = new PipelineTestScript(sh:sh)
        provisioner = new NoOpProvisioner(script)

        ProvisionedHost host = provisioner.provision(new TargetHost(), new ProvisioningConfig())
        assert(host.error == EXCEPTION_MESSAGE)
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
        provisioner = new NoOpProvisioner(script)

        ProvisioningConfig config = new ProvisioningConfig()
        config.mode = Mode.JNLP
        ProvisionedHost host = new ProvisionedHost(initialized:true, connectedToMaster:true)

        provisioner.teardown(host, config)

        assert(script.testLog.count("Exception: ${EXCEPTION_MESSAGE}") == 1)
    }
}
