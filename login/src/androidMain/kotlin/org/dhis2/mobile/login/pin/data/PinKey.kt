package org.dhis2.mobile.login.pin.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

/**
 * Device-bound key used by [PinHasher].
 */
fun interface PinKey {
    /**
     * Returns the HMAC of [data], or null if the key does not exist and [createIfMissing] is false.
     */
    fun mac(
        data: ByteArray,
        createIfMissing: Boolean,
    ): ByteArray?
}

/**
 * [PinKey] backed by a non-exportable HMAC key in the Android Keystore.
 */
class AndroidKeystorePinKey : PinKey {
    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "dhis2_session_pin_key"
    }

    override fun mac(
        data: ByteArray,
        createIfMissing: Boolean,
    ): ByteArray? {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val key =
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
                ?: if (createIfMissing) generateKey() else return null
        return Mac.getInstance(key.algorithm).run {
            init(key)
            doFinal(data)
        }
    }

    private fun generateKey(): SecretKey =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, ANDROID_KEYSTORE).run {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN).build())
            generateKey()
        }
}
