package org.dhis2.mobile.commons.auth

fun interface OpenIdController {
    fun handleIntent(
        intent: Any,
        onResult: suspend (Result<Any>) -> Unit,
    )
}
