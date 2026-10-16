package org.dhis2.data.server

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import timber.log.Timber

const val FORCED_INVALID_REFRESH_TOKEN = "forced-expiry-invalid-refresh-token"
const val FORCED_INVALID_ACCESS_TOKEN = "forced-expiry-invalid-access-token"

private const val LOG_TAG = "ForceSessionExpiry"

private const val AUTHORIZATION_HEADER = "Authorization"
private const val REFRESH_TOKEN_GRANT = "grant_type=refresh_token"
private val REFRESH_TOKEN_PARAM = Regex("(^|&)refresh_token=[^&]*")
private const val TOKEN_PATH = "token"
private const val PING_PATH = "/api/ping"
private const val MAX_BODY_LENGTH = 8 * 1024L

/**
 * Development tool that makes the current session look expired, so the app can be exercised
 * without waiting for the 30 days of inactivity it normally takes.
 *
 * It does not fake any response: it spoils what the app sends, and lets the server reject it. An
 * ordinary call loses its authorization header, so the server answers 401 and the SDK tries to
 * refresh; that refresh goes out with an invalid refresh token, so the server answers 400 and the
 * SDK discards the stored tokens and reports an expired session. From there the app behaves
 * exactly as it does after a real expiry, and the switch turns itself off so renewing the session
 * — which uses a different grant — works normally.
 */
class ForceSessionExpiryInterceptor(
    private val isArmed: () -> Boolean,
    private val disarm: () -> Unit,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val armed = isArmed()
        Timber.tag(LOG_TAG).v("armed=%s %s", armed, request.url)
        if (!armed) return chain.proceed(request)

        // Every sync starts by asking whether the server is reachable, and a spoiled answer to
        // that question stops it before anything can report an expired session
        if (request.isServerCheck()) {
            Timber.tag(LOG_TAG).d("Leaving the server check untouched")
            return chain.proceed(request)
        }

        if (!request.isTokenRequest()) {
            Timber.tag(LOG_TAG).d("Spoiling the access token of %s", request.url)
            return chain.proceed(request.withInvalidAccessToken()).also { response ->
                Timber.tag(LOG_TAG).d("%s answered %d", request.url, response.code)
            }
        }

        // The login exchange uses the same endpoint with another grant, and breaking it would
        // leave the user unable to renew the session
        val body = request.readBody()
        if (body?.contains(REFRESH_TOKEN_GRANT) != true) {
            Timber.tag(LOG_TAG).d("Leaving the login exchange untouched")
            return chain.proceed(request)
        }

        disarm()
        Timber.tag(LOG_TAG).d("Sending an invalid refresh token, the session will expire")
        return chain.proceed(request.withInvalidRefreshToken(body))
    }

    private fun Request.isServerCheck(): Boolean = url.encodedPath.endsWith(PING_PATH)

    /**
     * Recognised by the endpoint rather than by the content type of the body: the SDK talks Ktor,
     * whose OkHttp bodies do not always carry one.
     */
    private fun Request.isTokenRequest(): Boolean = method == "POST" && url.encodedPath.contains(TOKEN_PATH, ignoreCase = true)

    private fun Request.readBody(): String? =
        body?.takeIf { it.contentLength() in 0..MAX_BODY_LENGTH }?.let { requestBody ->
            Buffer().use { buffer ->
                requestBody.writeTo(buffer)
                buffer.readUtf8()
            }
        }

    private fun Request.withInvalidRefreshToken(body: String): Request {
        val spoiled =
            body.replace(REFRESH_TOKEN_PARAM) { match ->
                "${match.groupValues[1]}refresh_token=$FORCED_INVALID_REFRESH_TOKEN"
            }
        return newBuilder()
            .method(method, spoiled.toRequestBody(this.body?.contentType()))
            .build()
    }

    /**
     * An invalid token is answered with a plain 401, which is what the SDK reacts to. A missing
     * header can instead be answered with a redirect to the login page, and nothing is refreshed.
     */
    private fun Request.withInvalidAccessToken(): Request =
        newBuilder()
            .header(AUTHORIZATION_HEADER, "Bearer $FORCED_INVALID_ACCESS_TOKEN")
            .build()
}
