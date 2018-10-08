package com.redhat.ci.hosts

import org.junit.Test

/**
 * Tests if a ProvisionedHost can be created.
 */
class ProvisionedHostTest {
    private static final String ID = '1'
    private static final String ARCH = 'x86_64'
    private static final String HOSTNAME = 'test-host'
    private static final String TYPE = 'VM'
    private static final List<String> TYPE_PRIORITY = [TYPE]
    private static final String PROVIDER = 'BEAKER'
    private static final List<String> PROVIDER_PRIORITY = [PROVIDER]
    private static final String PROVISIONER = 'LINCHPIN'
    private static final List<String> PROVISIONER_PRIORITY = [PROVISIONER]

    @Test
    void canCreateProvisionedHost() {
        ProvisionedHost host = new ProvisionedHost()
        assert(host != null)
    }

    @Test
    void canCreateAProvisionedHostFromATargetHost() {
        TargetHost target = new TargetHost(
            id:ID,
            arch:ARCH,
            hostname:HOSTNAME,
            type:TYPE,
            typePriority:TYPE_PRIORITY,
            provider:PROVIDER,
            providerPriority:PROVIDER_PRIORITY,
            provisioner:PROVISIONER,
            provisionerPriority:PROVISIONER_PRIORITY
        )

        ProvisionedHost host = new ProvisionedHost(target)
        assert(host.id == ID)
        assert(host.arch == ARCH)
        assert(host.hostname == HOSTNAME)
        assert(host.type == TYPE)
        assert(host.typePriority == TYPE_PRIORITY)
        assert(host.provider == PROVIDER)
        assert(host.providerPriority == PROVIDER_PRIORITY)
        assert(host.provisioner == PROVISIONER)
        assert(host.provisionerPriority == PROVISIONER_PRIORITY)
    }
}
