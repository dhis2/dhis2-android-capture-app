package org.dhis2.mobile.login.main.domain.model

data class OpenIdLoginConfiguration(
    val serverUrl: String,
    val isNetworkAvailable: Boolean,
    val clientId: String,
    val redirectUri: String,
    val discoveryUri: String?,
    val authorizationUri: String?,
    val tokenUrl: String?,
    val prompt: String?,
)
