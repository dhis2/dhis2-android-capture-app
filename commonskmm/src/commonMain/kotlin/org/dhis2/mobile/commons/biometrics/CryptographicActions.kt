package org.dhis2.mobile.commons.biometrics

import javax.crypto.Cipher

interface CryptographicActions {
    fun getInitializedCipherForEncryption(): Cipher?

    fun getInitializedCipherForDecryption(initializationVector: ByteArray): Cipher?

    fun encryptData(
        plaintext: String,
        cipher: Cipher,
    ): CiphertextWrapper

    fun decryptData(
        ciphertext: ByteArray,
        cipher: Cipher,
    ): String

    fun isKeyReady(): Boolean

    fun deleteInvalidKey()
}
