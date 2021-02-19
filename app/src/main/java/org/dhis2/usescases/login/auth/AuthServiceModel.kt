package org.dhis2.usescases.login.auth

import android.net.Uri
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig

enum class AuthType {
    OAUTH2, OPENID
}

data class AuthServiceModel(
    val authType: AuthType,
    val loginLabel: String?,
    val clientId: String,
    val redirectUri: Uri?,
    val discoveryUri: Uri?,
    val scope: String?,
    val openIDConnectConfig: OpenIDConnectConfig?
) {
    companion object {
        fun mocked(): AuthServiceModel {
            return AuthServiceModel(
                AuthType.OPENID,
                "Log in with openid",
                "1019417002544-mqa7flk4mjohrgsbg9bta9bvluoj85o0.apps.googleusercontent.com",
                Uri.parse("com.googleusercontent.apps.1019417002544-mqa7flk4mjohrgsbg9bta9bvluoj85o0:/oauth2redirect"),
                Uri.parse("https://accounts.google.com/.well-known/openid-configuration"),
                "openid email profile",
                null
            )
        }
    }
}