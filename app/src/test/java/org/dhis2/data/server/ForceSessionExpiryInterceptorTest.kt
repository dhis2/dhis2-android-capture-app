package org.dhis2.data.server

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private const val FORM = "application/x-www-form-urlencoded"
private const val TOKEN_URL = "https://test.server.org/oauth2/token"
private const val API_URL = "https://test.server.org/api/me"

class ForceSessionExpiryInterceptorTest {
    private val chain: Interceptor.Chain = mock()
    private var armed = true
    private var disarmed = false

    private val interceptor =
        ForceSessionExpiryInterceptor(
            isArmed = { armed },
            disarm = { disarmed = true },
        )

    @Test
    fun `should leave requests untouched when the switch is off`() {
        // GIVEN
        armed = false
        val request = apiRequest()

        // WHEN
        val proceeded = proceed(request)

        // THEN - the dev tool is inert unless somebody turns it on
        assertEquals("Bearer token", proceeded.header("Authorization"))
    }

    @Test
    fun `should spoil the access token so the server rejects the call`() {
        // GIVEN - an ordinary api call with a still valid access token
        val request = apiRequest()

        // WHEN
        val proceeded = proceed(request)

        // THEN - an invalid token earns a clean 401, which is what makes the SDK try to refresh.
        // Removing the header instead can be answered with a redirect, which refreshes nothing
        assertEquals("Bearer $FORCED_INVALID_ACCESS_TOKEN", proceeded.header("Authorization"))
    }

    @Test
    fun `should leave the server check alone`() {
        // GIVEN - the ping every sync starts with, to decide whether the server is reachable
        val request =
            Request
                .Builder()
                .url("http://test.server.org/api/ping")
                .header("Authorization", "Bearer token")
                .build()

        // WHEN
        val proceeded = proceed(request)

        // THEN - spoiling it makes the sync give up as "server not available" before reaching
        // anything that could report an expired session
        assertEquals("Bearer token", proceeded.header("Authorization"))
    }

    @Test
    fun `should spoil the refresh token so the server rejects the refresh`() {
        // GIVEN - the refresh the SDK sends after that 401
        val request = tokenRequest("grant_type=refresh_token&refresh_token=real-token&client_id=abc")

        // WHEN
        val proceeded = proceed(request)

        // THEN - the server answers 400, the SDK discards the tokens and reports an expired
        // session, exactly as it does after 30 days of inactivity
        val body = proceeded.bodyAsString()
        assertTrue(body.contains("grant_type=refresh_token"))
        assertTrue(body.contains("refresh_token=$FORCED_INVALID_REFRESH_TOKEN"))
        assertTrue(!body.contains("real-token"))

        // AND - it disarms itself, so renewing the session afterwards works normally
        assertTrue(disarmed)
    }

    @Test
    fun `should leave the login exchange alone`() {
        // GIVEN - the authorization code exchange of a login, which uses the same endpoint
        val request = tokenRequest("grant_type=authorization_code&code=abc&client_id=abc")

        // WHEN
        val proceeded = proceed(request)

        // THEN - breaking this would stop the user from ever renewing the session
        assertEquals(
            "grant_type=authorization_code&code=abc&client_id=abc",
            proceeded.bodyAsString(),
        )
        assertTrue(!disarmed)
    }

    private fun apiRequest() =
        Request
            .Builder()
            .url(API_URL)
            .header("Authorization", "Bearer token")
            .build()

    private fun tokenRequest(body: String) =
        Request
            .Builder()
            .url(TOKEN_URL)
            .post(body.toRequestBody(FORM.toMediaType()))
            .build()

    private fun proceed(request: Request): Request {
        whenever(chain.request()) doReturn request
        val captor = argumentCaptor<Request>()
        whenever(chain.proceed(captor.capture())) doReturn emptyResponse(request)

        interceptor.intercept(chain)

        return captor.firstValue
    }

    private fun emptyResponse(request: Request) =
        Response
            .Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("".toResponseBody(null))
            .build()

    private fun Request.bodyAsString(): String {
        val buffer = Buffer()
        body?.writeTo(buffer)
        return buffer.readUtf8()
    }

    @Test
    fun `should spoil a refresh whose body carries no content type`() {
        // GIVEN - the SDK talks Ktor, whose OkHttp bodies often have no content type of their own
        val request =
            Request
                .Builder()
                .url(TOKEN_URL)
                .header("Content-Type", FORM)
                .post("grant_type=refresh_token&refresh_token=real-token".toRequestBody(null))
                .build()

        // WHEN
        val proceeded = proceed(request)

        // THEN - the refresh is still recognised, otherwise it would succeed and nothing would
        // ever look expired
        assertTrue(proceeded.bodyAsString().contains("refresh_token=$FORCED_INVALID_REFRESH_TOKEN"))
        assertTrue(disarmed)
    }

    @Test
    fun `should not read the body of requests that are not token calls`() {
        // GIVEN - an upload, whose body may be large and readable only once
        val request =
            Request
                .Builder()
                .url("https://test.server.org/api/tracker")
                .header("Authorization", "Bearer token")
                .post("{\"events\":[]}".toRequestBody("application/json".toMediaType()))
                .build()

        // WHEN
        val proceeded = proceed(request)

        // THEN - only the header is touched, the body travels as it was
        assertEquals("Bearer $FORCED_INVALID_ACCESS_TOKEN", proceeded.header("Authorization"))
        assertEquals("{\"events\":[]}", proceeded.bodyAsString())
    }
}
