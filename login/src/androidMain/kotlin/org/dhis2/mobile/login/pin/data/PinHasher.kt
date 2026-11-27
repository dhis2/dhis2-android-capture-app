package org.dhis2.mobile.login.pin.data

import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.io.encoding.Base64

/**
 * Derives and verifies the value stored for the session PIN: a random salt and the HMAC of the
 * salted PIN with [pinKey], a non-exportable Keystore key. Without that key a copy of the database
 * cannot be used to check any PIN, so a short PIN cannot be brute-forced offline.
 *
 * The value is stored as `$hmac-sha256$<salt>$<mac>`. Any other value is a PIN stored in plaintext
 * by a previous version: it can still be verified, and [Verification.Match.needsUpgrade] asks the
 * caller to replace it.
 */
class PinHasher(
    private val pinKey: PinKey,
) {
    private companion object {
        const val PREFIX = "\$hmac-sha256\$"
        const val SEPARATOR = '$'
        const val SALT_LENGTH_BYTES = 16
    }

    sealed interface Verification {
        data class Match(
            val needsUpgrade: Boolean,
        ) : Verification

        data object Mismatch : Verification

        /** The Keystore key is gone, so the stored value can no longer be verified. */
        data object KeyMissing : Verification
    }

    /**
     * Derives the value to store for [pin], creating the Keystore key if needed. Every call uses a
     * fresh salt, so the result is only valid for storing, never for comparing.
     */
    fun hash(pin: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val mac = requireNotNull(pinKey.mac(salt + pin.encodeToByteArray(), createIfMissing = true))
        return PREFIX + Base64.encode(salt) + SEPARATOR + Base64.encode(mac)
    }

    /**
     * Verifies [pin] against [storedValue], comparing in constant time.
     */
    @Suppress("ReturnCount")
    fun verify(
        pin: String,
        storedValue: String,
    ): Verification {
        if (!storedValue.startsWith(PREFIX)) {
            return if (MessageDigest.isEqual(pin.encodeToByteArray(), storedValue.encodeToByteArray())) {
                Verification.Match(needsUpgrade = true)
            } else {
                Verification.Mismatch
            }
        }

        val parts = storedValue.removePrefix(PREFIX).split(SEPARATOR)
        if (parts.size != 2) return Verification.Mismatch
        val salt = parts[0].decodeBase64OrNull() ?: return Verification.Mismatch
        val storedMac = parts[1].decodeBase64OrNull() ?: return Verification.Mismatch
        val mac =
            pinKey.mac(salt + pin.encodeToByteArray(), createIfMissing = false)
                ?: return Verification.KeyMissing

        return if (MessageDigest.isEqual(mac, storedMac)) {
            Verification.Match(needsUpgrade = false)
        } else {
            Verification.Mismatch
        }
    }

    private fun String.decodeBase64OrNull(): ByteArray? =
        try {
            Base64.decode(this)
        } catch (_: IllegalArgumentException) {
            null
        }
}
