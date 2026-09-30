package org.dhis2.data.server

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForcedSessionExpiryTest {
    @After
    fun tearDown() {
        ForcedSessionExpiry.disarm()
    }

    @Test
    fun `should start disarmed`() {
        // The tool only does something when a developer asks for it
        assertFalse(ForcedSessionExpiry.isArmed)
    }

    @Test
    fun `should stay armed until the session is spoiled`() {
        // GIVEN - the developer tapped the button
        ForcedSessionExpiry.arm()

        // THEN - it survives the calls leading to the refresh, which is what the interceptor
        // spoils, and only then is it consumed
        assertTrue(ForcedSessionExpiry.isArmed)
        assertTrue(ForcedSessionExpiry.isArmed)

        ForcedSessionExpiry.disarm()
        assertFalse(ForcedSessionExpiry.isArmed)
    }
}
