package com.redhat.ci

import org.junit.Test
import org.junit.Before
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.ProvisioningConfig

/**
 * Tests the install script wrapper.
 */
class UtilsTest {
    private static final String INSTALLED = 'Installed'
    private static final String TEST_HOSTNAME = 'test-host'
    private static final String NODE_STEP = 'node'
    private ProvisionedHost host = null
    private ProvisioningConfig config = null
    private PipelineTestScript script = null

    private final Closure genericInstall = {
        host ->
        script.sh(INSTALLED)
    }

    private final Closure node = {
        name, body ->
        script.echo(NODE_STEP)
        script.echo(name)
        body()
    }

    private final Closure sh = {
        sh ->
        script.echo(INSTALLED)
    }

    @Before
    void init() {
        host = new ProvisionedHost()
        config = new ProvisioningConfig()
        script = new PipelineTestScript(node:node, sh:sh)
    }

    @Test
    void shouldInstallAnsibleOnProvisionedHost() {
        assert(host.ansibleInstalled == false)
        Utils.installAnsible(script, host)
        assert(host.ansibleInstalled == true)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void shouldInstallAnsibleOnCurrentHost() {
        Utils.installAnsible(script)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void shouldInstallCredentialsOnProvisionedHost() {
        assert(host.credentialsInstalled == false)
        Utils.installCredentials(script, config, host)
        assert(host.credentialsInstalled == true)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void shouldInstallCredentialsOnCurrentHost() {
        Utils.installCredentials(script, config)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void shouldInstallRhpkgOnProvisionedHost() {
        assert(host.rhpkgInstalled == false)
        Utils.installRhpkg(script, host)
        assert(host.rhpkgInstalled == true)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void shouldInstallRhpkgOnCurrentHost() {
        Utils.installRhpkg(script)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void installWrapperShouldntWrapNullHost() {
        Utils.installWrapper(script, null, genericInstall)
        assert(script.testLog.contains(INSTALLED))
        assert(!script.testLog.contains(NODE_STEP))
    }

    @Test
    void installWrapperShouldntWrapNamelessHost() {
        Utils.installWrapper(script, host, genericInstall)
        assert(script.testLog.contains(INSTALLED))
        assert(!script.testLog.contains(NODE_STEP))
    }

    @Test
    void installWrapperShouldWrapNamedHost() {
        host.displayName = TEST_HOSTNAME
        Utils.installWrapper(script, host, genericInstall)
        assert(script.testLog.contains(INSTALLED))
        assert(script.testLog.contains(NODE_STEP))
        assert(script.testLog.contains(TEST_HOSTNAME))
    }
}
