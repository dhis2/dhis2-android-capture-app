package org.dhis2.mobile.login.main.domain.usecase

import coil3.PlatformContext
import org.dhis2.mobile.commons.biometrics.BiometricActions
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.login.main.data.LoginErrorMessageProvider
import org.dhis2.mobile.login.main.data.LoginRepository

class UpdateBiometricPermission(
    private val loginRepository: LoginRepository,
    private val preferences: PreferenceProvider,
    private val biometrics: BiometricActions,
    private val messages: LoginErrorMessageProvider,
) {
    context(platformContext: PlatformContext)
    suspend operator fun invoke(
        serverUrl: String,
        username: String,
        password: String,
        granted: Boolean,
    ): Result<Unit> =
        when {
            preferences.areSameCredentials(serverUrl, username) ->
                Result.failure(Exception(messages.biometricsAlreadySaved()))

            granted && biometrics.hasBiometric() && biometrics.hasProtectedSecret().not() ->
                biometrics
                    .protectSecret(password)
                    .fold(
                        onSuccess = { saveCredentials(serverUrl, username, password = null, granted) },
                        onFailure = { Result.failure(Exception(messages.biometricsCancelled())) },
                    )

            else -> saveCredentials(serverUrl, username, password, granted)
        }

    /** A null [password] means it is kept by [biometrics] instead of the preferences. */
    private suspend fun saveCredentials(
        serverUrl: String,
        username: String,
        password: String?,
        granted: Boolean,
    ): Result<Unit> {
        preferences.saveUserCredentials(serverUrl, username, password)
        loginRepository.updateBiometricsPermissions(granted)
        return Result.success(Unit)
    }
}
