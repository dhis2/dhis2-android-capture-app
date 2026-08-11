package org.dhis2.mobile.login.main.ui.navigation

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@ExperimentalCoroutinesApi
class AppLinkNavigationTest {
    @Test
    fun `GIVEN a URL was emitted before any subscription WHEN a collector subscribes THEN it receives the buffered value once`() =
        runTest {
            val appLinkNavigation = AppLinkNavigation()
            val emittedUrl = "https://example.org/callback?code=123"

            appLinkNavigation.emit(emittedUrl)

            appLinkNavigation.appLink.test {
                assertEquals(emittedUrl, awaitItem())
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN a collector is subscribed WHEN a URL is emitted THEN it receives the value once`() =
        runTest {
            val appLinkNavigation = AppLinkNavigation()
            val emittedUrl = "https://example.org/callback?code=123"

            appLinkNavigation.appLink.test {
                appLinkNavigation.emit(emittedUrl)
                assertEquals(emittedUrl, awaitItem())
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN a URL was emitted and consumed WHEN a second collector subscribes later THEN it receives no value`() =
        runTest {
            val appLinkNavigation = AppLinkNavigation()
            val emittedUrl = "https://example.org/callback?code=123"

            appLinkNavigation.appLink.test {
                appLinkNavigation.emit(emittedUrl)
                assertEquals(emittedUrl, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            appLinkNavigation.appLink.test(timeout = 100.milliseconds) {
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }
}
