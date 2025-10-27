package org.dhis2.mobile.login.pin.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.junit.Before
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertTrue

class GetInitialScreenUseCaseTest {
    private lateinit var useCase: GetInitialScreen
    private val repository: AccountRepository = mock()

    @Before
    fun setUp() {
        useCase = GetInitialScreen(repository)
    }

    @Test
    fun `go to server validation if there is no logged account`() =
        runTest {
            // Given
            whenever(repository.getLoggedInAccounts()) doReturn emptyList()
            whenever(repository.availableServers()) doReturn emptyList()

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.ServerValidation)
        }

    @Test
    fun `go to login screen if there is 1 logged account`() =
        runTest {
            // Given
            whenever(repository.getLoggedInAccounts()) doReturn
                listOf(accountModel)

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.LegacyLogin)
        }

    @Test
    fun `go to oauth if there is 1 logged account with oauth`() =
        runTest {
            // Given
            whenever(repository.getLoggedInAccounts()) doReturn
                listOf(accountModel.copy(isOauthEnabled = true))

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.OauthLogin)
        }

    @Test
    fun `go to account if there is some logged accounts`() =
        runTest {
            // Given
            whenever(repository.getLoggedInAccounts()) doReturn
                listOf(accountModel, accountModel, accountModel)

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.Accounts)
        }

    val accountModel =
        AccountModel(
            name = "name",
            serverUrl = "serverUrl",
            serverName = "serverName",
            serverDescription = "description",
            serverFlag = null,
            allowRecovery = true,
            oidcIcon = null,
            oidcLoginText = null,
            oidcUrl = null,
            isOauthEnabled = false,
        )
}
