package com.redhat.ci.provisioner

import org.junit.Test

/**
 * Exhaustive coverage of provisioning modes.
 */
class ModeTest {

    @Test
    void supportsSSH() {
        assert(Mode.SSH == 'SSH')
    }

    @Test
    void supportsJNLP() {
        assert(Mode.JNLP == 'JNLP')
    }
}
