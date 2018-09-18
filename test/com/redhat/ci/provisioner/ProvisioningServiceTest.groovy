package com.redhat.ci.provisioner

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.when
import static org.mockito.Mockito.thenReturn
import static org.mockito.Mockito.thenThrow
import static org.mockito.Mockito.thenCallRealMethod
import static org.mockito.Mockito.any
import static com.redhat.ci.provider.Type.BEAKER

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import com.redhat.ci.hosts.TargetHost

/**
 * Tests individual methods in the ProvisioningService.
 */
@RunWith(MockitoJUnitRunner)
class ProvisioningServiceTest {
    private final Script script = new PipelineTestScript()

    private final ProvisioningService provSvc = new ProvisioningService()
    private final ProvisioningConfig config = new ProvisioningConfig()

    @Test
    void invalidProvisionerRequestThrowsException() {
        Boolean exceptionThrown = false
        TargetHost target = new TargetHost()
        target.provisioner = 'FAKE'
        try {
            provSvc.provision(target, config, script)
        } catch (ProvisionerUnavailableException e) {
            exceptionThrown = true
        }

        assert(exceptionThrown)
    }

    @Test
    @SuppressWarnings('CatchNullPointerException')
    void provisionFailureThrowsException() {
        TargetHost target = new TargetHost(provider:BEAKER, provisioner:Type.LINCHPIN)
        String provisionerType = Type.LINCHPIN
        ProvisioningService mockSvc = spy(provSvc)
        Provisioner mockProv = mock(Provisioner)

        when(mockSvc.provision(target, config, script)).thenCallRealMethod()
        when(mockSvc.getProvisioner(provisionerType, script)).thenReturn(mockProv)
        when(mockProv.available).thenReturn(true)
        when(mockProv.supportsProvider(any())).thenReturn(true)
        when(mockProv.supportsHostType(any())).thenReturn(true)
        when(mockProv.provision(target, config))
            .thenThrow(new NullPointerException('This is exceptional')).thenReturn(null)

        Boolean exceptionThrown = false
        try {
            mockSvc.provision(target, config, script)
        } catch (ProvisionerUnavailableException e) {
            exceptionThrown = true
        }

        assert(exceptionThrown)
    }
}
