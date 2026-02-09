package org.dhis2.mobile.login.main.domain.model

sealed interface ServerValidationResult {
    data class Success(
        val serverName: String?,
        val serverDescription: String?,
        val countryFlag: String?,
        val allowRecovery: Boolean,
        val oidcIcon: String?,
        val oidcLoginText: String?,
        val oidcUrl: String?,
        val oAuthEnabled: Boolean,
    ) : ServerValidationResult

    data class Error(
        val message: String,
    ) : ServerValidationResult
}
