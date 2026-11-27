package org.dhis2.usescases.main.data

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.configuration.internal.DatabaseAccount
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.server.LoginConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class HomeRepositoryImplTest {
    private val d2: D2 = mock(defaultAnswer = RETURNS_DEEP_STUBS)
    private val domainErrorMapper: DomainErrorMapper = mock()

    private fun repository(dispatcher: Dispatcher) =
        HomeRepositoryImpl(
            d2 = d2,
            charts = null,
            preferences = mock(),
            workManagerController = mock(),
            syncStatusController = mock(),
            domainErrorMapper = domainErrorMapper,
            dispatcher = dispatcher,
        )

    @Test
    fun `serverName returns the application title of the current account server`() =
        runTest {
            givenCurrentAccount(LoginConfig(applicationTitle = "Training"))

            assertEquals("Training", serverName())
        }

    @Test
    fun `serverName returns the server url if the server has no application title`() =
        runTest {
            givenCurrentAccount(LoginConfig(applicationTitle = null))

            assertEquals(SERVER_URL, serverName())
        }

    @Test
    fun `serverName returns the server url if the application title is blank`() =
        runTest {
            givenCurrentAccount(LoginConfig(applicationTitle = " "))

            assertEquals(SERVER_URL, serverName())
        }

    @Test
    fun `serverName returns the server url if the login config is not downloaded yet`() =
        runTest {
            givenCurrentAccount(loginConfig = null)

            assertEquals(SERVER_URL, serverName())
        }

    @Test
    fun `serverName returns null if there is no current account`() =
        runTest {
            whenever(d2.userModule().accountManager().getCurrentAccount()) doReturn null

            val serverName =
                repository(Dispatcher(io = UnconfinedTestDispatcher(testScheduler))).serverName()

            assertNull(serverName)
        }

    @Test
    fun `serverName maps SDK errors to domain errors`() =
        runTest {
            val d2Error =
                D2Error
                    .builder()
                    .errorCode(D2ErrorCode.UNEXPECTED)
                    .errorDescription("")
                    .build()
            val domainError = DomainError.DatabaseError("Error")
            whenever(d2.userModule().accountManager().getCurrentAccount()) doAnswer { throw d2Error }
            whenever(domainErrorMapper.mapToDomainError(d2Error)) doReturn domainError
            val repository = repository(Dispatcher(io = UnconfinedTestDispatcher(testScheduler)))

            val error =
                try {
                    repository.serverName()
                    null
                } catch (exception: DomainError) {
                    exception
                }

            assertEquals(domainError, error)
        }

    private suspend fun TestScope.serverName() = repository(Dispatcher(io = UnconfinedTestDispatcher(testScheduler))).serverName()

    private fun givenCurrentAccount(loginConfig: LoginConfig?) {
        val account =
            DatabaseAccount(
                username = "user",
                serverUrl = SERVER_URL,
                databaseName = "database",
                databaseCreationDate = Date(),
                lastAccessDate = null,
                encrypted = false,
                syncState = null,
                importDB = null,
                loginConfig = loginConfig,
                authorizationType = null,
            )
        whenever(d2.userModule().accountManager().getCurrentAccount()) doReturn account
    }

    private companion object {
        const val SERVER_URL = "https://play.dhis2.org"
    }
}
