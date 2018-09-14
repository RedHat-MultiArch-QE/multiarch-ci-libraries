package com.redhat.ci

import org.junit.Test
import org.junit.Before
import com.redhat.ci.hosts.ProvisionedHost
import com.redhat.ci.provisioner.ProvisioningConfig

/**
 * Tests the install script wrapper.
 */
class UtilsTest extends PipelineTestScript {
    private ProvisionedHost host = null
    private ProvisioningConfig config = null

    private final Closure genericInstall = {
        host ->
    }

    @Before
    void init() {
        host = new ProvisionedHost()
        config = new ProvisioningConfig()
    }

    @Test
    void shouldInstallAnsibleOnProvisionedHost() {
        assert(host.ansibleInstalled == false)
        assert(methodCallCounts['sh'] == 0)
        Utils.installAnsible(this, host)
        assert(host.ansibleInstalled == true)
        assert(methodCallCounts['sh'] == 1)
    }

    @Test
    void shouldInstallAnsibleOnCurrentHost() {
        assert(methodCallCounts['sh'] == 0)
        Utils.installAnsible(this)
        assert(methodCallCounts['sh'] == 1)
    }

    @Test
    void shouldInstallCredentialsOnProvisionedHost() {
        assert(host.credentialsInstalled == false)
        assert(methodCallCounts['sh'] == 0)
        assert(methodCallCounts['withCredentials'] == 0)
        Utils.installCredentials(this, config, host)
        assert(host.credentialsInstalled == true)
        assert(methodCallCounts['sh'] == 1)
        assert(methodCallCounts['withCredentials'] == 1)
    }

    @Test
    void shouldInstallCredentialsOnCurrentHost() {
        assert(methodCallCounts['sh'] == 0)
        assert(methodCallCounts['withCredentials'] == 0)
        Utils.installCredentials(this, config)
        assert(methodCallCounts['sh'] == 1)
        assert(methodCallCounts['withCredentials'] == 1)
    }

    @Test
    void shouldInstallRhpkgOnProvisionedHost() {
        assert(host.rhpkgInstalled == false)
        assert(methodCallCounts['sh'] == 0)
        Utils.installRhpkg(this, host)
        assert(host.rhpkgInstalled == true)
        assert(methodCallCounts['sh'] == 1)
    }

    @Test
    void shouldInstallRhpkgOnCurrentHost() {
        assert(methodCallCounts['sh'] == 0)
        Utils.installRhpkg(this)
        assert(methodCallCounts['sh'] == 1)
    }

    @Test
    void installWrapperShouldntWrapNullHost() {
        Utils.installWrapper(this, null, genericInstall)
        assert(methodCallCounts['node'] == 0)
    }

    @Test
    void installWrapperShouldntWrapNamelessHost() {
        Utils.installWrapper(this, host, genericInstall)
        assert(methodCallCounts['node'] == 0)
    }

    @Test
    void installWrapperShouldWrapNamedHost() {
        host.displayName = 'test-host'
        Utils.installWrapper(this, host, genericInstall)
        assert(methodCallCounts['node'] == 1)
    }
}
