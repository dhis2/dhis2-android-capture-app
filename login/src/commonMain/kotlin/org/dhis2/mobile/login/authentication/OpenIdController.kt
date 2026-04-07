package org.dhis2.mobile.login.authentication

interface OpenIdController {
    fun handleIntent(
        intent: Any,
        onResult: suspend (Result<Any>) -> Unit,
    )

    fun bind(context: Any)
}
