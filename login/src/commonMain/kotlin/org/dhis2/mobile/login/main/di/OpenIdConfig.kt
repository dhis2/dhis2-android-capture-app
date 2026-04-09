package org.dhis2.mobile.login.main.di

data class OpenIdConfig(
    val clientId: String = "",
    val redirectUri: String = "",
    val discoveryUri: String = "",
    val buttonText: String = "Login with open id",
    val server: String = "https://android.im.dhis2.org/dev",
)
