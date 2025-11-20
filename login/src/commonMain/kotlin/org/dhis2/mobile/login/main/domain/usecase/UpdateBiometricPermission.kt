package org.dhis2.mobile.login.main.domain.usecase

import coil3.PlatformContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.dhis2.mobile.commons.biometrics.BiometricActions
import org.dhis2.mobile.commons.biometrics.CryptographicActions
import org.dhis2.mobile.commons.logging.logDebug
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.biometrics_permission_already_saved
import org.jetbrains.compose.resources.getString
import kotlin.coroutines.resume

class UpdateBiometricPermission(
    private val loginRepository: LoginRepository,
    private val preferences: PreferenceProvider,
    private val biometrics: BiometricActions,
    private val cryptographics: CryptographicActions,
) {
    private companion object {
        const val TAG = "UpdateBiometricPermission"
    }

    context(platformContext: PlatformContext)
    suspend operator fun invoke(
        serverUrl: String,
        username: String,
        password: String,
        granted: Boolean,
    ): Result<Unit> =
        if (preferences.areSameCredentials(serverUrl, username).not()) {
            if (biometrics.hasBiometric() && cryptographics.isKeyReady().not() && granted) {
                suspendCancellableCoroutine { continuation ->
                    val scope = CoroutineScope(continuation.context)
                    cryptographics.getInitializedCipherForEncryption()?.let {
                        biometrics.authenticate(it) { cryptoObjectCipher ->
                            scope.launch {
                                val ciphertextWrapper =
                                    cryptographics.encryptData(password, cryptoObjectCipher)
                                preferences.saveUserCredentialsAndCipher(
                                    serverUrl,
                                    username,
                                    ciphertextWrapper,
                                )
                                loginRepository.updateBiometricsPermissions(granted)
                                if (continuation.isActive) {
                                    continuation.resume(Result.success(Unit))
                                }
                            }
                        }
                    }
                    continuation.invokeOnCancellation {
                        logDebug(TAG, "Biometric authentication cancelled")
                    }
                }
            } else {
                preferences.saveUserCredentials(
                    serverUrl,
                    username,
                    password,
                )
                loginRepository.updateBiometricsPermissions(granted)
                Result.success(Unit)
            }
        } else {
            Result.failure(Exception(getString(Res.string.biometrics_permission_already_saved)))
        }
}
