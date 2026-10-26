package org.dhis2.mobile.app.splash.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.app.splash.domain.model.StartDestination
import org.dhis2.mobile.login.main.domain.usecase.IsUserLoggedIn
import org.dhis2.mobile.login.main.domain.usecase.VerifyNeedOfflineCode
import org.dhis2.mobile.login.pin.domain.usecase.GetIsSessionLockedUseCase
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetStartDestinationTest {
    private val isUserLoggedIn: IsUserLoggedIn = mock()
    private val verifyNeedOfflineCode: VerifyNeedOfflineCode = mock()
    private val getIsSessionLocked: GetIsSessionLockedUseCase = mock()

    private val getStartDestination =
        GetStartDestination(isUserLoggedIn, verifyNeedOfflineCode, getIsSessionLocked)

    @Test
    fun `GIVEN no logged in user WHEN invoked THEN it starts in login`() =
        runTest {
            whenever(isUserLoggedIn(Unit)) doReturn Result.success(false)

            val result = getStartDestination(Unit)

            assertEquals(StartDestination.Login, result.getOrThrow())
        }

    @Test
    fun `GIVEN a logged in user with an unlocked session WHEN invoked THEN it starts in home`() =
        runTest {
            whenever(isUserLoggedIn(Unit)) doReturn Result.success(true)
            whenever(verifyNeedOfflineCode(Unit)) doReturn Result.success(false)
            whenever(getIsSessionLocked(false)) doReturn false

            val result = getStartDestination(Unit)

            assertEquals(StartDestination.Home, result.getOrThrow())
        }

    @Test
    fun `GIVEN a logged in user with a locked session WHEN invoked THEN it starts in login`() =
        runTest {
            whenever(isUserLoggedIn(Unit)) doReturn Result.success(true)
            whenever(verifyNeedOfflineCode(Unit)) doReturn Result.success(false)
            whenever(getIsSessionLocked(false)) doReturn true

            val result = getStartDestination(Unit)

            assertEquals(StartDestination.Login, result.getOrThrow())
        }

    @Test
    fun `GIVEN a logged in user needing an offline code WHEN invoked THEN it starts in login`() =
        runTest {
            whenever(isUserLoggedIn(Unit)) doReturn Result.success(true)
            whenever(verifyNeedOfflineCode(Unit)) doReturn Result.success(true)
            whenever(getIsSessionLocked(true)) doReturn true

            val result = getStartDestination(Unit)

            assertEquals(StartDestination.Login, result.getOrThrow())
        }

    @Test
    fun `GIVEN the session check fails WHEN invoked THEN the failure is returned`() =
        runTest {
            whenever(isUserLoggedIn(Unit)) doReturn Result.failure(Exception("cannot check session"))

            val result = getStartDestination(Unit)

            assertTrue(result.isFailure)
        }
}
