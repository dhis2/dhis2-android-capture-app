package org.dhis2.data.biometric

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import coil3.PlatformContext
import org.dhis2.R
import org.dhis2.mobile.commons.biometrics.BiometricActions
import org.dhis2.mobile.commons.biometrics.CiphertextWrapper
import org.dhis2.mobile.commons.biometrics.CryptographicActions
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEY_SIZE_BITS = 256
private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
const val KEY_NAME = "DHIS2_BIOMETRIC_KEY"

class BiometricAuthenticator(
    private val context: Context,
) : BiometricActions {
    override fun hasBiometric(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    context(context: PlatformContext)
    override fun authenticate(
        cipher: Cipher,
        callback: (Cipher) -> Unit,
    ) {
        val cryptoObject = BiometricPrompt.CryptoObject(cipher)
        authenticate(
            context as FragmentActivity,
            {
                it.cryptoObject?.cipher?.let { cipher -> callback(cipher) }
            },
            cryptoObject,
        )
    }

    fun authenticate(
        fragmentActivity: FragmentActivity,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        cryptoObject: BiometricPrompt.CryptoObject? = null,
    ) {
        // Always create a fresh BiometricPrompt to avoid reusing old callbacks
        val biometricPrompt =
            BiometricPrompt(
                fragmentActivity,
                ContextCompat.getMainExecutor(context),
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
                .setTitle(context.getString(R.string.biometric_title))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText(context.getString(R.string.use_password))
                .build()
        if (cryptoObject == null) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        }
    }
}

class CryptographyManager : CryptographicActions {
    private val keyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

    override fun getInitializedCipherForEncryption(): Cipher? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cipher = getCipher()
            val secretKey =
                getOrCreateSecretKey()

            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            cipher
        } else {
            null
        }

    override fun getInitializedCipherForDecryption(initializationVector: ByteArray): Cipher? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cipher = getCipher()
            val secretKey = getOrCreateSecretKey()
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
            cipher
        } else {
            null
        }

    override fun encryptData(
        plaintext: String,
        cipher: Cipher,
    ): CiphertextWrapper {
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return CiphertextWrapper(ciphertext, cipher.iv)
    }

    override fun decryptData(
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

    @RequiresApi(Build.VERSION_CODES.M)
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

    override fun isKeyReady(): Boolean = keyStore.getKey(KEY_NAME, null) != null

    override fun deleteInvalidKey() {
        if (isKeyReady()) {
            keyStore.deleteEntry(KEY_NAME)
        }
    }
}
