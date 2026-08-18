package org.dhis2.mobile.login.main.domain.model

sealed interface LoginResult {
    data class Success(
        val displayTrackingMessage: Boolean,
        val initialSyncDone: Boolean,
    ) : LoginResult

    data class Error(
        val message: String?,
        val attemptsLeft: Int? = null,
    ) : LoginResult

    data class LockOut(
        val lockoutSeconds: Int,
    ) : LoginResult
}
