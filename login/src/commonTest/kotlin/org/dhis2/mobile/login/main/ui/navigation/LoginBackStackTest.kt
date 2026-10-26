package org.dhis2.mobile.login.main.ui.navigation

import androidx.navigation3.runtime.NavKey
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginBackStackTest {
    private val serverValidation =
        LoginScreenState.ServerValidation(
            currentServer = "",
            availableServers = emptyList(),
            hasAccounts = false,
        )

    private val recoverAccount = LoginScreenState.RecoverAccount(selectedServer = "https://server")

    @Test
    fun `GIVEN the loading screen WHEN navigating THEN the destination replaces it`() {
        val backStack = mutableListOf<NavKey>(LoginScreenState.Loading)

        backStack.navigateTo(serverValidation)

        assertEquals(listOf<NavKey>(serverValidation), backStack)
    }

    @Test
    fun `GIVEN a screen without loading WHEN navigating THEN the destination is added on top`() {
        val backStack = mutableListOf<NavKey>(serverValidation)

        backStack.navigateTo(LoginScreenState.Accounts)

        assertEquals(listOf<NavKey>(serverValidation, LoginScreenState.Accounts), backStack)
    }

    @Test
    fun `GIVEN screens above loading WHEN navigating THEN loading and everything above it is removed`() {
        val backStack = mutableListOf<NavKey>(serverValidation, LoginScreenState.Loading, LoginScreenState.Accounts)

        backStack.navigateTo(recoverAccount)

        assertEquals(listOf<NavKey>(serverValidation, recoverAccount), backStack)
    }

    @Test
    fun `GIVEN more than one screen WHEN navigating up THEN the top screen is removed`() {
        val backStack = mutableListOf<NavKey>(serverValidation, LoginScreenState.Accounts)

        val navigated = backStack.navigateUp()

        assertTrue(navigated)
        assertEquals(listOf<NavKey>(serverValidation), backStack)
    }

    @Test
    fun `GIVEN a single screen WHEN navigating up THEN it stays and nothing is navigated`() {
        val backStack = mutableListOf<NavKey>(serverValidation)

        val navigated = backStack.navigateUp()

        assertFalse(navigated)
        assertEquals(listOf<NavKey>(serverValidation), backStack)
    }
}
