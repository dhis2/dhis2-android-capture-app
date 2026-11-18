package org.dhis2.data.biometric

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import coil3.PlatformContext
import kotlinx.coroutines.suspendCancellableCoroutine
import org.dhis2.R
import org.dhis2.mobile.commons.biometrics.BiometricActions
import org.dhis2.mobile.commons.biometrics.CiphertextWrapper
import org.dhis2.mobile.commons.providers.BIOMETRIC_CREDENTIALS
import org.dhis2.mobile.commons.providers.PreferenceProvider
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.coroutines.resume

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEY_SIZE_BITS = 256
private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
const val KEY_NAME = "DHIS2_BIOMETRIC_KEY"

/**
 * Android [BiometricActions]: the secret is encrypted with a Keystore key that only the biometric
 * prompt can unlock, and the ciphertext is kept in the preferences.
 */
class BiometricAuthenticator(
    private val appContext: Context,
    private val cryptographyManager: CryptographyManager,
    private val preferences: PreferenceProvider,
) : BiometricActions {
    override fun hasBiometric(): Boolean {
        val biometricManager = BiometricManager.from(appContext)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    override fun hasProtectedSecret(): Boolean = cryptographyManager.isKeyReady()

    context(context: PlatformContext)
    override suspend fun protectSecret(secret: String): Result<Unit> =
        authenticateWith(cryptographyManager.getInitializedCipherForEncryption()).map { cipher ->
            preferences.saveAsJson(BIOMETRIC_CREDENTIALS, cryptographyManager.encryptData(secret, cipher))
        }

    context(context: PlatformContext)
    override suspend fun readSecret(): Result<String> {
        val ciphertextWrapper =
            preferences.getBiometricCredentials()
                ?: return Result.failure(Exception("No biometrics found"))
        val cipher = cryptographyManager.getInitializedCipherForDecryption(ciphertextWrapper.initializationVector)
        return authenticateWith(cipher).map { authenticatedCipher ->
            cryptographyManager.decryptData(ciphertextWrapper.ciphertext, authenticatedCipher)
        }
    }

    override fun deleteSecret() {
        preferences.removeValue(BIOMETRIC_CREDENTIALS)
        cryptographyManager.deleteInvalidKey()
    }

    /** Resumes only when the user authenticates; a dismissed prompt leaves the caller waiting. */
    context(context: PlatformContext)
    private suspend fun authenticateWith(cipher: Cipher): Result<Cipher> =
        suspendCancellableCoroutine { continuation ->
            authenticate(
                context as FragmentActivity,
                { result ->
                    result.cryptoObject?.cipher?.let { continuation.resume(Result.success(it)) }
                },
                BiometricPrompt.CryptoObject(cipher),
            )
        }

    private fun authenticate(
        fragmentActivity: FragmentActivity,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        cryptoObject: BiometricPrompt.CryptoObject? = null,
    ) {
        // Always create a fresh BiometricPrompt to avoid reusing old callbacks
        val biometricPrompt =
            BiometricPrompt(
                fragmentActivity,
                ContextCompat.getMainExecutor(appContext),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess(result)
                    }
                },
            )

        val promptInfo =
            BiometricPrompt.PromptInfo
                .Builder()
                .setTitle(appContext.getString(R.string.biometric_title))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText(appContext.getString(R.string.use_password))
                .build()
        if (cryptoObject == null) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        }
    }
}

class CryptographyManager {
    private val keyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

    fun getInitializedCipherForEncryption(): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    fun getInitializedCipherForDecryption(initializationVector: ByteArray): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        return cipher
    }

    fun encryptData(
        plaintext: String,
        cipher: Cipher,
    ): CiphertextWrapper {
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return CiphertextWrapper(ciphertext, cipher.iv)
    }

    fun decryptData(
        ciphertext: ByteArray,
        cipher: Cipher,
    ): String {
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charset.forName("UTF-8"))
    }

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        // If Secretkey was previously created for that keyName, then grab and return it.
        keyStore.getKey(KEY_NAME, null)?.let { return it as SecretKey }

        // if you reach here, then a new SecretKey must be generated for that keyName
        val paramsBuilder =
            KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE_BITS)
            setUserAuthenticationRequired(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setInvalidatedByBiometricEnrollment(true)
            }
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE,
            )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    fun isKeyReady(): Boolean = keyStore.getKey(KEY_NAME, null) != null

    fun deleteInvalidKey() {
        if (isKeyReady()) {
            keyStore.deleteEntry(KEY_NAME)
        }
    }
}
