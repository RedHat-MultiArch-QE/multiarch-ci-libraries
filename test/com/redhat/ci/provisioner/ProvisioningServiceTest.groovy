package com.redhat.ci.provisioner

import static org.mockito.Mockito.spy
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import static org.mockito.Mockito.thenReturn
import static org.mockito.Mockito.thenThrow
import static org.mockito.Mockito.anyString
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.times
import static com.redhat.ci.provider.Type.BEAKER
import static com.redhat.ci.host.Type.BAREMETAL

import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import com.redhat.ci.hosts.TargetHost
import com.redhat.ci.hosts.ProvisionedHost

/**
 * Tests individual methods in the ProvisioningService.
 */
@RunWith(MockitoJUnitRunner)
class ProvisioningServiceTest {
    private static final String EXCEPTION_MESSAGE = 'This is exceptional.'

    private final Script script = new PipelineTestScript()
    private final ProvisioningConfig config = new ProvisioningConfig()

    private ProvisioningService mockSvc

    @Before
    void init() {
        mockSvc = spy(new ProvisioningService())
    }

    @Test
    void invalidProvisionerRequestThrowsException() {
        TargetHost target = new TargetHost(provisioner:'FAKE')

        Boolean exceptionThrown = false
        try {
            mockSvc.provision(target, config, script)
        } catch (ProvisioningException e) {
            exceptionThrown = true
            assert(e.message == ProvisioningService.UNAVAILABLE)
        }

        assert(exceptionThrown)
        assert(script.testLog.contains("Unrecognized provisioner:${target.provisioner}"))
    }

    @Test
    void provisionFailureThrowsException() {
        TargetHost target = new TargetHost(type:BAREMETAL, provider:BEAKER, provisioner:Type.LINCHPIN)

        Provisioner mockProv = mock(Provisioner)
        when(mockProv.provision(target, config))
            .thenThrow(new NullPointerException(EXCEPTION_MESSAGE))
            .thenReturn(null)
        when(mockProv.available).thenReturn(true)
        when(mockProv.supportsProvider(anyString())).thenReturn(true)
        when(mockProv.supportsHostType(anyString())).thenReturn(true)
        when(mockSvc.getProvisioner(target.provisioner, script)).thenReturn(mockProv)

        Boolean exceptionThrown = false
        try {
            mockSvc.provision(target, config, script)
        } catch (ProvisioningException e) {
            exceptionThrown = true
        }

        assert(exceptionThrown)
        assert(script.testLog.contains("Exception: ${EXCEPTION_MESSAGE}"))
        assert(script.testLog.contains("Provisioning ${target.type} " +
                                       "host with ${target.provisioner} provisioner " +
                                       "and ${target.provider} provider failed."))
    }

    @Test
    void teardownShouldPassthroughToProvisionerTeardown() {
        TargetHost target = new TargetHost(type:BAREMETAL, provider:BEAKER, provisioner:Type.LINCHPIN)
        ProvisionedHost host = new ProvisionedHost(target)

        Provisioner mockProv = mock(Provisioner)
        when(mockSvc.getProvisioner(target.provisioner, script)).thenReturn(mockProv)

        mockSvc.teardown(host, config, script)

        verify(mockProv, times(1)).teardown(host, config)
    }

    @Test
    void openShiftProvisionerIsUnavailable() {
        TargetHost target = new TargetHost(provisioner:Type.OPENSHIFT)

        Boolean exceptionThrown = false
        try {
            mockSvc.provision(target, config, script)
        } catch (ProvisioningException e) {
            exceptionThrown = true
            assert(e.message == ProvisioningService.UNAVAILABLE)
        }

        assert(exceptionThrown)

        verify(mockSvc, times(1)).provision(target, config, script)
    }

    @Test
    void kubeVirtProvisionerIsUnavailable() {
        TargetHost target = new TargetHost(provisioner:Type.KUBEVIRT)

        Boolean exceptionThrown = false
        try {
            mockSvc.provision(target, config, script)
        } catch (ProvisioningException e) {
            exceptionThrown = true
            assert(e.message == ProvisioningService.UNAVAILABLE)
        }

        assert(exceptionThrown)

        verify(mockSvc, times(1)).provision(target, config, script)
    }

    @Test
    void linchpinProvisionerIsAvailable() {
        TargetHost target = new TargetHost(provisioner:Type.LINCHPIN)

        mockSvc.provision(target, config, script)

        verify(mockSvc, times(1)).provision(target, config, script)
    }

    @Test
    void defaultTargetProvisionsAndTearsdownSuccessfully() {
        TargetHost target = new TargetHost()
        ProvisionedHost host = new ProvisionedHost(type:BAREMETAL, provisioner:Type.LINCHPIN, provider:BEAKER)

        Provisioner mockProv = mock(Provisioner)
        when(mockProv.provision(target, config)).thenReturn(host)
        when(mockProv.available).thenReturn(true)
        when(mockProv.supportsProvider(BEAKER)).thenReturn(true)
        when(mockProv.supportsHostType(BAREMETAL)).thenReturn(true)
        when(mockSvc.getProvisioner(Type.LINCHPIN, script)).thenReturn(mockProv)

        ProvisionedHost provisionedHost = mockSvc.provision(target, config, script)
        assert(provisionedHost != null)

        mockSvc.teardown(provisionedHost, config, script)

        verify(mockSvc, times(1)).provision(target, config, script)
        verify(mockSvc, times(1)).teardown(host, config, script)
        verify(mockProv, times(1)).provision(target, config)
        verify(mockProv, times(1)).teardown(host, config)
    }
}
