package org.dhis2.mobile.login.main.domain

import io.ktor.http.Url

const val OAUTH_REDIRECT_SCHEME = "dhis2oauth"
const val OAUTH_REDIRECT_HOST = "oauth"

sealed interface OAuthCallback {
    data class Error(
        val code: String?,
    ) : OAuthCallback

    data class Enrollment(
        val iat: String,
        val state: String,
    ) : OAuthCallback

    data class Login(
        val code: String,
        val state: String,
    ) : OAuthCallback

    data object Other : OAuthCallback

    data object Invalid : OAuthCallback
}

object OAuthCallbackParser {
    private const val MAX_VALUE_LENGTH = 2048
    private val TOKEN_REGEX = Regex("^[A-Za-z0-9._~+/=-]{1,$MAX_VALUE_LENGTH}$")

    private val KNOWN_ERRORS =
        setOf(
            "access_denied",
            "invalid_request",
            "unauthorized_client",
            "unsupported_response_type",
            "invalid_scope",
            "server_error",
            "temporarily_unavailable",
            "interaction_required",
            "login_required",
            "consent_required",
            "account_selection_required",
        )

    fun isOAuthRedirect(
        scheme: String?,
        host: String?,
    ): Boolean =
        scheme.equals(OAUTH_REDIRECT_SCHEME, ignoreCase = true) &&
            host.equals(OAUTH_REDIRECT_HOST, ignoreCase = true)

    fun parse(url: String): OAuthCallback {
        val parsed = runCatching { Url(url) }.getOrNull() ?: return OAuthCallback.Invalid
        if (!isOAuthRedirect(parsed.protocol.name, parsed.host)) return OAuthCallback.Invalid

        val params = parsed.parameters
        if (params.names().any { params.getAll(it).orEmpty().size > 1 }) return OAuthCallback.Invalid
        val state = params["state"]?.takeIf(::isValidToken)

        params["error"]?.let { error ->
            return OAuthCallback.Error(error.takeIf { it in KNOWN_ERRORS })
        }
        params["iat"]?.let { iat ->
            if (!isValidToken(iat) || state == null) return OAuthCallback.Invalid
            return OAuthCallback.Enrollment(iat, state)
        }
        params["code"]?.let { code ->
            if (!isValidToken(code) || state == null) return OAuthCallback.Invalid
            return OAuthCallback.Login(code, state)
        }
        return OAuthCallback.Other
    }

    private fun isValidToken(value: String) = TOKEN_REGEX.matches(value)
}
