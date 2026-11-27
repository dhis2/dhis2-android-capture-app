package org.dhis2.mobile.login.main.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class OAuthCallbackParserTest {
    @Test
    fun `login callback is parsed`() {
        assertEquals(
            OAuthCallback.Login("abc", "xyz"),
            OAuthCallbackParser.parse("dhis2oauth://oauth?code=abc&state=xyz"),
        )
    }

    @Test
    fun `session_state is not taken as state`() {
        assertEquals(
            OAuthCallback.Login("abc", "real"),
            OAuthCallbackParser.parse("dhis2oauth://oauth?session_state=fake&code=abc&state=real"),
        )
    }

    @Test
    fun `error_description alone is not an error`() {
        assertEquals(
            OAuthCallback.Other,
            OAuthCallbackParser.parse("dhis2oauth://oauth?error_description=hacked"),
        )
    }

    @Test
    fun `unknown error code is not exposed`() {
        assertEquals(
            OAuthCallback.Error(null),
            OAuthCallbackParser.parse("dhis2oauth://oauth?error=Your%20account%20is%20locked"),
        )
    }

    @Test
    fun `known error code is kept`() {
        assertEquals(
            OAuthCallback.Error("access_denied"),
            OAuthCallbackParser.parse("dhis2oauth://oauth?error=access_denied&state=x"),
        )
    }

    @Test
    fun `wrong scheme or host is invalid`() {
        assertEquals(OAuthCallback.Invalid, OAuthCallbackParser.parse("evil://oauth?code=a&state=b"))
        assertEquals(OAuthCallback.Invalid, OAuthCallbackParser.parse("dhis2oauth://evil?code=a&state=b"))
        assertEquals(OAuthCallback.Invalid, OAuthCallbackParser.parse("https://test.redirect.org?code=a&state=b"))
    }

    @Test
    fun `invalid characters or oversized values are rejected`() {
        assertEquals(OAuthCallback.Invalid, OAuthCallbackParser.parse("dhis2oauth://oauth?code=a%3Cscript%3E&state=b"))
        assertEquals(OAuthCallback.Invalid, OAuthCallbackParser.parse("dhis2oauth://oauth?code=${"a".repeat(2049)}&state=b"))
    }

    @Test
    fun `percent encoded values are decoded`() {
        assertEquals(
            OAuthCallback.Enrollment("a/b", "s"),
            OAuthCallbackParser.parse("dhis2oauth://oauth?iat=a%2Fb&state=s"),
        )
    }
}
