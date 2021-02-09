package org.dhis2.usescases.login.auth

import android.net.Uri

enum class AuthType {
    OAUTH2, OPENID
}

data class AuthServiceModel(
    val authType: AuthType,
    val loginLabel: String?,
    val clientId: String,
    val redirectUri: Uri?,
    val discoveryUri: Uri?,
    val scope:String?
) {
    companion object {
        fun mocked(): AuthServiceModel {
            return AuthServiceModel(
                AuthType.OPENID,
                "Log in with openid",
                "652593417937-ouaqob8mol887jmcbvvb8m54b22sp9g3.apps.googleusercontent.com",
//                Uri.parse("com.googleusercontent.apps.652593417937-ouaqob8mol887jmcbvvb8m54b22sp9g3:/oauth2redirect"),
                Uri.parse("org.dhis2:/oauth2redirect"),
                Uri.parse("https://accounts.google.com/.well-known/openid-configuration"),
                "openid email profile"
            )
        }
    }
}