package org.dhis2.mobile.login.main.domain.model

sealed interface ServerValidationResult {
    object Legacy : ServerValidationResult

    object Oauth : ServerValidationResult

    data class Error(
        val message: String,
    ) : ServerValidationResult
}
