package org.dhis2.mobile.login.pin.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PinHasherTest {
    private companion object {
        const val PIN = "4826"
    }

    private val pinHasher = PinHasher(fakePinKey())

    @Test
    fun `hash does not contain the PIN and uses a fresh salt every time`() {
        val hash = pinHasher.hash(PIN)

        assertTrue(hash.startsWith("\$hmac-sha256\$"))
        assertFalse(hash.contains(PIN))
        assertNotEquals(hash, pinHasher.hash(PIN))
    }

    @Test
    fun `verify matches only the PIN that was hashed`() {
        val hash = pinHasher.hash(PIN)

        assertEquals(PinHasher.Verification.Match(needsUpgrade = false), pinHasher.verify(PIN, hash))
        assertEquals(PinHasher.Verification.Mismatch, pinHasher.verify("0000", hash))
    }

    @Test
    fun `verify rejects the right PIN with the key of another device`() {
        val otherDevice = PinHasher(fakePinKey(deviceSecret = "other-device-secret"))

        assertEquals(PinHasher.Verification.Mismatch, otherDevice.verify(PIN, pinHasher.hash(PIN)))
    }

    @Test
    fun `verify reports a missing key without creating one`() {
        var keyCreated = false
        val noKey = PinHasher(PinKey { _, createIfMissing -> null.also { keyCreated = createIfMissing } })

        assertEquals(PinHasher.Verification.KeyMissing, noKey.verify(PIN, pinHasher.hash(PIN)))
        assertFalse(keyCreated)
    }

    @Test
    fun `verify matches a legacy plaintext PIN and asks for an upgrade`() {
        assertEquals(PinHasher.Verification.Match(needsUpgrade = true), pinHasher.verify(PIN, PIN))
        assertEquals(PinHasher.Verification.Mismatch, pinHasher.verify("0000", PIN))
    }

    @Test
    fun `verify rejects a corrupted stored value`() {
        listOf(
            "\$hmac-sha256\$not-base64!\$aGFzaA==",
            "\$hmac-sha256\$c2FsdA==",
            "\$hmac-sha256\$c2FsdA==\$aGFzaA==\$extra",
        ).forEach { value ->
            assertEquals(PinHasher.Verification.Mismatch, pinHasher.verify(PIN, value), value)
        }
    }
}
