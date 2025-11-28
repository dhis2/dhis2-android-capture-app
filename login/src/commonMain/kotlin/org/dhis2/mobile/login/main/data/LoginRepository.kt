package org.dhis2.mobile.login.main.data

import coil3.PlatformContext
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
typealias UserPassword = String

interface LoginRepository {
    suspend fun validateServer(
        server: String,
        isNetworkAvailable: Boolean,
    ): ServerValidationResult

    suspend fun loginUser(
        serverUrl: String,
        username: String,
        password: String,
        isNetworkAvailable: Boolean,
    ): Result<Unit>

    suspend fun getAvailableLoginUsernames(): List<String>

    suspend fun unlockSession()

    suspend fun updateAvailableUsers(username: String)

    suspend fun updateServerUrls(serverUrl: String)

    suspend fun displayTrackingMessage(): Boolean

    suspend fun initialSyncDone(
        serverUrl: String,
        username: String,
    ): Boolean

    suspend fun canLoginWithBiometrics(serverUrl: String): Boolean

    suspend fun displayBiometricMessage(): Boolean

    suspend fun hasOtherAccounts(): Boolean

    suspend fun numberOfAccounts(): Int

    suspend fun updateTrackingPermissions(granted: Boolean)

    suspend fun updateBiometricsPermissions(granted: Boolean)

    context(context: PlatformContext)
    suspend fun loginWithBiometric(): Result<UserPassword>

    suspend fun deleteBiometricCredentials()

    suspend fun importDatabase(path: String): Result<Unit>

    suspend fun loginWithOpenId(
        serverUrl: String,
        isNetworkAvailable: Boolean,
        clientId: String,
        redirectUri: String,
        discoveryUri: String?,
        authorizationUri: String?,
        tokenUrl: String?,
    ): Result<Unit>

    suspend fun getUsername(): String

    suspend fun logoutUser(): Result<Unit>
}
