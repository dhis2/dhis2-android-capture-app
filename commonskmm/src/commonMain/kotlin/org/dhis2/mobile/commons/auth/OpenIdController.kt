package org.dhis2.mobile.commons.auth

interface OpenIdController {
    fun handleIntent(
        intent: Any,
        onResult: suspend (Any) -> Unit,
    )
}
