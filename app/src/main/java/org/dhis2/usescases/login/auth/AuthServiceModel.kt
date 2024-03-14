package org.dhis2.usescases.login.auth

import android.net.Uri
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig

data class AuthServiceModel(
    val serverUrl: String?,
    val loginLabel: String?,
    val clientId: String,
    val redirectUri: String,
    val discoveryUri: String?,
    val authorizationUrl: String?,
    val tokenUrl: String?,
) {
    fun toOpenIdConfig(): OpenIDConnectConfig {
        return OpenIDConnectConfig(
            clientId,
            Uri.parse(redirectUri),
            discoveryUri?.let { Uri.parse(discoveryUri) },
            authorizationUrl,
            tokenUrl,
        )
    }

    fun hasConfiguration(): Boolean {
        return discoveryUri != null || (authorizationUrl != null && tokenUrl != null)
    }
}
