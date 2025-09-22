package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.biometrics.BiometricActions
import org.dhis2.mobile.commons.biometrics.CryptographicActions
import org.dhis2.mobile.commons.providers.PreferenceProvider

class UpdateBiometricPermission(
    private val preferences: PreferenceProvider,
    private val biometrics: BiometricActions,
    private val cryptographics: CryptographicActions,
) {
    suspend operator fun invoke(
        serverUrl: String,
        username: String,
        password: String,
        granted: Boolean
    ) {
        if (preferences.areSameCredentials(serverUrl, username).not()) {
            val pass = if (granted) password else serverUrl
            if (biometrics.hasBiometric() && cryptographics.isKeyReady().not()) {

                cryptographics.getInitializedCipherForEncryption()?.let {
                    biometrics.authenticate(it) { cryptoObjectCipher ->
                        val ciphertextWrapper = cryptographics.encryptData(pass, cryptoObjectCipher)
                        preferences.saveUserCredentialsAndCipher(
                            serverUrl,
                            username,
                            ciphertextWrapper
                        )
                    }
                }
            }
            preferences.saveUserCredentials(
                serverUrl,
                username,
                password
            )
        }
    }
}