package org.dhis2.mobile.login.main.data

import coil3.PlatformContext
import org.dhis2.mobile.login.main.domain.model.OpenIdLoginConfiguration
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult

/**
 * [LoginRepository] for targets without the DHIS2 Android SDK. It answers what is needed to reach
 * the server validation screen with no logged-in user; validating a server fails with an error.
 */
internal class FakeLoginRepository : LoginRepository {
    override suspend fun validateServer(
        server: String,
        isNetworkAvailable: Boolean,
    ): ServerValidationResult = ServerValidationResult.Error(NOT_SUPPORTED_MESSAGE)

    override suspend fun isUserLoggedIn(): Result<Boolean> = Result.success(false)

    override suspend fun needsOfflineCode(): Result<Boolean> = Result.success(false)

    override suspend fun loginUser(
        serverUrl: String,
        username: String,
        password: String,
    ): Result<Unit> = notSupportedResult()

    override suspend fun getAvailableLoginUsernames(): List<String> = emptyList()

    override suspend fun unlockSession() = notSupported()

    override suspend fun updateAvailableUsers(username: String) = notSupported()

    override suspend fun updateServerUrls(serverUrl: String) = notSupported()

    override suspend fun displayTrackingMessage(): Boolean = false

    override suspend fun initialSyncDone(
        serverUrl: String,
        username: String,
    ): Boolean = false

    override suspend fun canLoginWithBiometrics(serverUrl: String): Boolean = false

    override suspend fun displayBiometricMessage(): Boolean = false

    override suspend fun hasOtherAccounts(): Boolean = false

    override suspend fun numberOfAccounts(): Int = 0

    override suspend fun updateTrackingPermissions(granted: Boolean) = notSupported()

    override suspend fun updateBiometricsPermissions(granted: Boolean) = notSupported()

    context(context: PlatformContext)
    override suspend fun loginWithBiometric(): Result<UserPassword> = notSupportedResult()

    override suspend fun deleteBiometricCredentials() = notSupported()

    override suspend fun importDatabase(path: String): Result<Unit> = notSupportedResult()

    override suspend fun loginWithOpenId(openIdLoginConfiguration: OpenIdLoginConfiguration): Result<Unit> = notSupportedResult()

    override suspend fun getUsername(): String = notSupported()

    override suspend fun logoutUser(): Result<Unit> = notSupportedResult()

    override suspend fun getDeviceEnrollmentUrl(serverUrl: String): String = notSupported()

    override suspend fun isDeviceRegistered(): Boolean = false

    override suspend fun getAuthorizationUrl(serverUrl: String): String = notSupported()

    override suspend fun enrollDevice(
        iat: String,
        serverURL: String,
        state: String,
    ): String = notSupported()

    override suspend fun loginUserWithOAuth(
        serverUrl: String,
        code: String,
        state: String,
        expectedUsername: String?,
    ): Result<String?> = notSupportedResult()

    override suspend fun buildLogoutUrl(serverUrl: String): String = notSupported()

    override suspend fun setOfflineCode(code: String): Result<Unit> = notSupportedResult()

    private fun notSupported(): Nothing = throw UnsupportedOperationException(NOT_SUPPORTED_MESSAGE)

    private fun <T> notSupportedResult(): Result<T> = Result.failure(UnsupportedOperationException(NOT_SUPPORTED_MESSAGE))

    private companion object {
        const val NOT_SUPPORTED_MESSAGE = "Login is not available on this platform yet"
    }
}
