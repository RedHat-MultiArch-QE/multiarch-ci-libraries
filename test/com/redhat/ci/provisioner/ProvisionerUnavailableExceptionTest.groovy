package com.redhat.ci.provisioner

import org.junit.Test
import org.junit.Before

/**
 * Tests ensuring that ProvisionerUnavailableExceptions can be thrown.
 */
class ProvisionerUnavailableExceptionTest {

    private static final String MESSAGE = 'Provisioner is unavailable.'
    private Boolean exceptionThrown = null
    private String exceptionMessage = null

    @Before
    void init() {
        exceptionThrown = false
        exceptionMessage = null
    }

    @Test
    void throwsEmptyException() {
        try {
            throw new ProvisionerUnavailableException()
        } catch (ProvisionerUnavailableException e) {
            exceptionThrown = true
            exceptionMessage = e.message
        }

        assert(exceptionThrown == true)
        assert(!exceptionMessage)
    }

    @Test
    void throwsMessageException() {
        try {
            throw new ProvisionerUnavailableException(MESSAGE)
        } catch (ProvisionerUnavailableException e) {
            exceptionThrown = true
            exceptionMessage = e.message
        }

        assert(exceptionThrown == true)
        assert(exceptionMessage == MESSAGE)
    }
}
