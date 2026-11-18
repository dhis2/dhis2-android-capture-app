package org.dhis2.mobile.commons.biometrics

import coil3.PlatformContext

/**
 * Keeps a secret, the user's password, that can only be read back after a biometric check.
 *
 * How the secret is protected is up to each platform: Android encrypts it with a Keystore key
 * unlocked by the biometric prompt, iOS would keep it in the Keychain behind biometric access control.
 */
interface BiometricActions {
    /** Whether the device has strong biometrics enrolled. */
    fun hasBiometric(): Boolean

    /** Whether a secret is currently protected by biometrics. */
    fun hasProtectedSecret(): Boolean

    /** Shows the biometric prompt and protects [secret] behind it. */
    context(context: PlatformContext)
    suspend fun protectSecret(secret: String): Result<Unit>

    /** Shows the biometric prompt and returns the protected secret. */
    context(context: PlatformContext)
    suspend fun readSecret(): Result<String>

    /** Forgets the protected secret and the key that protects it. */
    fun deleteSecret()
}
