package com.redhat.ci

import org.junit.Test
import org.junit.Before
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.Mode

/**
 * Tests the install script wrapper.
 */
class UtilsTest {
    private static final String SUDO = 'sudo'
    private static final String INSTALLED = 'Installed'
    private static final String TEST_HOSTNAME = 'test-host'
    private static final String NODE_STEP = 'node'
    private ProvisionedHost validHost = null
    private ProvisionedHost invalidHost = null
    private ProvisioningConfig config = null
    private PipelineTestScript script = null

    private final Closure installWrapper = {
        sudo, sh ->
        if (sudo) {
            script.echo(SUDO)
        }
        sh(script, null)
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
        validHost = new ProvisionedHost(hostname:TEST_HOSTNAME, displayName:TEST_HOSTNAME)
        invalidHost = new ProvisionedHost()
        config = new ProvisioningConfig()
        script = new PipelineTestScript(node:node, sh:sh)
    }

    @Test
    void shouldInstallAnsibleOnProvisionedHost() {
        assert(validHost.ansibleInstalled == false)
        Utils.installAnsible(script, config, validHost)
        assert(validHost.ansibleInstalled == true)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void shouldInstallAnsibleOnCurrentHost() {
        Utils.installAnsible(script, config)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void shouldInstallCredentialsOnProvisionedHost() {
        assert(validHost.credentialsInstalled == false)
        Utils.installCredentials(script, config, validHost)
        assert(validHost.credentialsInstalled == true)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void shouldInstallCredentialsOnCurrentHost() {
        Utils.installCredentials(script, config)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void shouldInstallRhpkgOnProvisionedHost() {
        assert(validHost.rhpkgInstalled == false)
        Utils.installRhpkg(script, config, validHost)
        assert(validHost.rhpkgInstalled == true)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void shouldInstallRhpkgOnCurrentHost() {
        Utils.installRhpkg(script, config)
        assert(script.testLog.contains(INSTALLED))
    }

    @Test
    void genericInstallShouldntWrapNullHost() {
        Utils.genericInstall(config, null, installWrapper)
        assert(script.testLog.contains(INSTALLED))
        assert(!script.testLog.contains(NODE_STEP))
    }

    @Test
    void genericInstallShouldntInstallOnNamelessHostInSSHMode() {
        config.mode = Mode.SSH
        Boolean exceptionOccured = false
        try {
            Utils.genericInstall(config, invalidHost, installWrapper)
        } catch (e) {
            exceptionOccured = true
        }
        assert(exceptionOccured)
    }

    @Test
    void genericInstallShouldntInstallOnNamelessHostInJNLPMode() {
        config.mode = Mode.JNLP
        Boolean exceptionOccured = false
        try {
            Utils.genericInstall(config, invalidHost, installWrapper)
        } catch (e) {
            exceptionOccured = true
        }
        assert(exceptionOccured)
    }

    @Test
    void genericInstallShouldntWrapNamedHostInSSHMode() {
        config.mode = Mode.SSH
        Utils.genericInstall(config, validHost, installWrapper)
        assert(script.testLog.contains(INSTALLED))
        assert(!script.testLog.contains(NODE_STEP))
    }

    @Test
    void genericInstallShouldWrapNamedHostInJNLPMode() {
        config.mode = Mode.JNLP
        Utils.genericInstall(config, validHost, installWrapper)
        assert(script.testLog.contains(INSTALLED))
        assert(script.testLog.contains(NODE_STEP))
        assert(script.testLog.contains(TEST_HOSTNAME))
    }

    @Test
    void genericInstallNotARealMode() {
        config.mode = 'FAKE'
        Utils.genericInstall(config, validHost, installWrapper)
        assert(!script.testLog.contains(INSTALLED))
        assert(!script.testLog.contains(NODE_STEP))
    }

    @Test
    void noSudoInSSHModeAsRoot() {
        config.mode = Mode.SSH
        Utils.genericInstall(config, validHost, installWrapper)
        assert(script.testLog.contains(INSTALLED))
        assert(!script.testLog.contains(SUDO))
    }

    @Test
    void sudoInSSHModeAsNonRoot() {
        config.mode = Mode.SSH
        validHost.remoteUser = 'custom'
        Utils.genericInstall(config, validHost, installWrapper)
        assert(script.testLog.contains(INSTALLED))
        assert(script.testLog.contains(SUDO))
    }
}
