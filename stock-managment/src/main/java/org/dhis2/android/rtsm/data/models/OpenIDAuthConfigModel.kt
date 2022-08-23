package org.dhis2.android.rtsm.data.models

import android.net.Uri
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig

data class OpenIDAuthConfigModel(
    val serverUrl: String?,
    val clientId: String,
    val redirectUri: String,
    val discoveryUri: String?,
    val authorizationUri: String?,
    val tokenUrl: String?
) {
    fun toOpenIDConnectionConfig(): OpenIDConnectConfig {
        return OpenIDConnectConfig(
            clientId,
            Uri.parse(redirectUri),
            discoveryUri?.let { Uri.parse(discoveryUri) },
            authorizationUri,
            tokenUrl
        )
    }
}
