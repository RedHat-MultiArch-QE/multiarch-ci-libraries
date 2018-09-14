package com.redhat.ci

import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.any
import static org.mockito.Mockito.eq

import org.junit.Test
import org.junit.Before
import com.redhat.ci.provisioner.ProvisioningConfig
import com.redhat.ci.provisioner.ProvisioningService
import com.redhat.ci.provisioner.Mode
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Tests the install job wrapper.
 */
@RunWith(MockitoJUnitRunner)
class JobTest extends Job {
    private Boolean onCompleteCalled = false

    private final Closure onCompleteNoOp = {
        onCompleteCalled = true
    }

    private final Closure bodyNoOp = {
        host, config ->
        script.echo('body')
    }

    private final Closure bodyFailure = {
        host, config ->
        script.echo('bodyFailure')
        throw new TestException('Test failed in body')
    }

    private final Closure onFailureNoOp = {
        error, host ->
        script.echo("onFailure(${error.message})")
    }

    private final TargetHost target = new TargetHost(arch:'x86_64')

    private final ProvisionedHost validHost = new ProvisionedHost(
        hostname:'test-host',
        arch:'x86_64',
        type:'VM',
        provisioner:'LINCHPIN',
        provider:'BEAKER'
    )

    @Before
    void init() {
        this.script = new PipelineTestScript()
        this.onCompleteCalled = false
        this.body = bodyNoOp
        this.config = new ProvisioningConfig()
        this.targetHosts = [target]
        this.provSvc = mock(ProvisioningService)
        this.onFailure = onFailureNoOp
        this.onComplete = onCompleteNoOp
    }

    JobTest() {
        super(
            null,
            null,
            null,
            null,
            null,
            null
        )
    }

    @Test
    void provisionNullHost() {
        when(provSvc.provision(target, config, script)).thenReturn(null)

        assert(script.methodCallCounts['echo'] == 0)
        ProvisionedHost host = provision(target)
        assert(script.methodCallCounts['echo'] == 1)
        assert(host == null)
        verify(provSvc, times(1)).provision(target, config, script)
    }

    @Test
    void provisionInvalidHostWithError() {
        ProvisionedHost invalidHost = new ProvisionedHost(target)
        invalidHost.error = 'This is a really bad error'
        when(provSvc.provision(target, config, script)).thenReturn(invalidHost)

        assert(script.methodCallCounts['echo'] == 0)
        ProvisionedHost host = provision(target)
        assert(script.methodCallCounts['echo'] == 2)
        assert(host != null)
        verify(provSvc, times(1)).provision(target, config, script)
    }

    @Test
    void provisionValidHost() {
        when(provSvc.provision(target, config, script)).thenReturn(validHost)

        assert(script.methodCallCounts['echo'] == 0)
        ProvisionedHost host = provision(target)
        assert(script.methodCallCounts['echo'] == 0)
        assert(host != null)
        verify(provSvc, times(1)).provision(target, config, script)
    }

    @Test
    void teardownNullHost() {
        doThrow(new NullPointerException('Null host cannot be torn down.'))
            .when(provSvc).teardown(null, config, script)

        assert(script.methodCallCounts['echo'] == 0)
        teardown(null)
        assert(script.methodCallCounts['echo'] == 1)
        verify(provSvc, times(1)).teardown(null, config, script)
    }

    @Test
    void teardownValidHost() {
        doNothing().when(provSvc).teardown(validHost, config, script)

        assert(script.methodCallCounts['echo'] == 0)
        teardown(validHost)
        assert(script.methodCallCounts['echo'] == 0)
        verify(provSvc, times(1)).teardown(validHost, config, script)
    }

    @Test
    void run() {
        when(provSvc.provision(target, config, script)).thenReturn(validHost)
        doNothing().when(provSvc).teardown(validHost, config, script)
        assert(script.methodCallCounts['parallel'] == 0)
        super.run()
        verify(provSvc, times(1)).provision(target, config, script)
        verify(provSvc, times(1)).teardown(validHost, config, script)
        assert(script.methodCallCounts['parallel'] == 1)
        assert(onCompleteCalled == true)
    }

    @Test
    void runOnTargetWithProvisionFailure() {
        when(provSvc.provision(target, config, script))
            .thenThrow(new NullPointerException('Null host cannot provisioned.'))
            .thenReturn(null)
        doNothing().when(provSvc).teardown(any(ProvisionedHost), eq(config), eq(script))
        runOnTarget(target)
        verify(provSvc, times(1)).provision(target, config, script)
        verify(provSvc, times(1)).teardown(any(ProvisionedHost), eq(config), eq(script))
    }

    @Test
    void runOnTargetWithBodyFailureOverJNLP() {
        this.config.mode = Mode.JNLP
        assert(config.mode == Mode.JNLP)
        this.body = bodyFailure

        when(provSvc.provision(target, config, script)).thenReturn(validHost)
        doNothing().when(provSvc).teardown(validHost, config, script)

        runOnTarget(target)

        verify(provSvc, times(1)).provision(target, config, script)
        verify(provSvc, times(1)).teardown(validHost, config, script)
    }

    @Test
    void runOnTargetWithBodyFailureOverSSH() {
        this.config.mode = Mode.SSH
        assert(config.mode == Mode.SSH)
        this.body = bodyFailure

        when(provSvc.provision(target, config, script)).thenReturn(validHost)
        doNothing().when(provSvc).teardown(validHost, config, script)

        runOnTarget(target)

        verify(provSvc, times(1)).provision(target, config, script)
        verify(provSvc, times(1)).teardown(validHost, config, script)
    }

    @Test
    void runOnTargetOverJNLP() {
        this.config.mode = Mode.JNLP
        assert(config.mode == Mode.JNLP)

        when(provSvc.provision(target, config, script)).thenReturn(validHost)
        doNothing().when(provSvc).teardown(validHost, config, script)

        runOnTarget(target)

        verify(provSvc, times(1)).provision(target, config, script)
        verify(provSvc, times(1)).teardown(validHost, config, script)
        assert(script.methodCallCounts['node'] == 2)
    }

    @Test
    void runOnTargetOverSSH() {
        this.config.mode = Mode.SSH
        assert(config.mode == Mode.SSH)

        when(provSvc.provision(target, config, script)).thenReturn(validHost)
        doNothing().when(provSvc).teardown(validHost, config, script)

        runOnTarget(target)

        verify(provSvc, times(1)).provision(target, config, script)
        verify(provSvc, times(1)).teardown(validHost, config, script)
        assert(script.methodCallCounts['node'] == 1)
    }
}
