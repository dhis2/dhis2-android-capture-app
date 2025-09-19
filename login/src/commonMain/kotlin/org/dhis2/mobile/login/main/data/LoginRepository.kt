package org.dhis2.mobile.login.main.data

import org.dhis2.mobile.login.main.domain.model.ServerValidationResult

interface LoginRepository {
    suspend fun validateServer(
        server: String,
        isNetworkAvailable: Boolean,
    ): ServerValidationResult
    suspend fun loginUser(serverUrl: String, username: String, password: String): Result<Unit>
    suspend fun getAvailableLoginUsernames(): List<String>
    suspend fun unlockSession()
    suspend fun updateAvailableUsers(username: String)
    suspend fun displayTrackingMessage(): Boolean
    suspend fun initialSyncDone(
        serverUrl: String,
        username: String,
    ): Boolean

    suspend fun canLoginWithBiometrics(serverUrl: String): Boolean
    suspend fun displayBiometricMessage(): Boolean
    fun updateTrackingPermissions(granted: Boolean)
}
