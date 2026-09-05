package org.dhis2.mobile.login.pin.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.mockito.Mockito
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

class SessionRepositoryImplTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val domainErrorMapper: DomainErrorMapper = mock()
    private val testDispatcher = StandardTestDispatcher()

    private val pin = "1234"

    private val d2Error =
        D2Error
            .builder()
            .errorCode(D2ErrorCode.OAUTH2_NO_VALID_TOKEN)
            .errorDescription("There is no access token")
            .build()

    private val mappedError = DomainError.SessionRenewalRequiredError("Your session has expired")

    // Resolved eagerly: navigating the deep stubs inside a doAnswer().whenever(...) argument
    // would run while Mockito is already stubbing, which it rejects
    private val pinValue = d2.dataStoreModule().localDataStore().value("pin")
    private val userModule = d2.userModule()

    private val repository =
        SessionRepositoryImpl(
            d2 = d2,
            preferenceProvider = mock<PreferenceProvider>(),
            domainErrorMapper = domainErrorMapper,
            dispatcher = Dispatcher(testDispatcher, testDispatcher, testDispatcher),
        )

    @BeforeTest
    fun setUp() {
        // runTest reuses the scheduler of the main dispatcher, so the repository and the test
        // share one clock
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN an SDK error on saving WHEN the PIN is saved THEN the mapped domain error is thrown`() =
        runTest {
            // GIVEN
            whenever(domainErrorMapper.mapToDomainError(d2Error)) doReturn mappedError
            doAnswer { throw d2Error }.whenever(pinValue).blockingSet(pin)

            // WHEN & THEN
            assertEquals(mappedError, thrownBy { repository.savePin(pin) })
        }

    @Test
    fun `GIVEN an SDK error rewrapped by RxJava on saving WHEN the PIN is saved THEN it is mapped as well`() =
        runTest {
            // GIVEN - D2Error is a checked exception, so the blocking operators the SDK exposes
            // rewrap it; the inline catch (d2Error: D2Error) this replaced never saw those
            whenever(domainErrorMapper.mapToDomainError(d2Error)) doReturn mappedError
            doAnswer { throw RuntimeException(d2Error) }.whenever(pinValue).blockingSet(pin)

            // WHEN & THEN
            assertEquals(mappedError, thrownBy { repository.savePin(pin) })
        }

    @Test
    fun `GIVEN an SDK error rewrapped by RxJava on deleting WHEN the PIN is deleted THEN it is mapped as well`() =
        runTest {
            // GIVEN
            whenever(domainErrorMapper.mapToDomainError(d2Error)) doReturn mappedError
            doAnswer { throw RuntimeException(d2Error) }.whenever(pinValue).blockingDeleteIfExist()

            // WHEN & THEN
            assertEquals(mappedError, thrownBy { repository.deletePin() })
        }

    @Test
    fun `GIVEN an SDK error rewrapped by RxJava on reading WHEN the PIN is read THEN it is mapped as well`() =
        runTest {
            // GIVEN
            whenever(domainErrorMapper.mapToDomainError(d2Error)) doReturn mappedError
            doAnswer { throw RuntimeException(d2Error) }.whenever(pinValue).blockingGet()

            // WHEN & THEN
            assertEquals(mappedError, thrownBy { repository.getStoredPin() })
        }

    @Test
    fun `GIVEN an expired session on logout WHEN the user is logged out THEN the renewal error is thrown`() =
        runTest {
            // GIVEN - this is the path "Forgot PIN" takes, so the renewal error has to survive it
            whenever(domainErrorMapper.mapToDomainError(d2Error)) doReturn mappedError
            doAnswer { throw RuntimeException(d2Error) }.whenever(userModule).blockingLogOut()

            // WHEN & THEN
            assertEquals(mappedError, thrownBy { repository.logout() })
        }

    @Test
    fun `GIVEN an unrelated failure WHEN the PIN is saved THEN it travels on untouched`() =
        runTest {
            // GIVEN - nothing to map, so nothing should be invented
            val failure = IllegalStateException("not an SDK error")
            doAnswer { throw failure }.whenever(pinValue).blockingSet(pin)

            // WHEN & THEN - the type and message survive; coroutine stack-trace recovery
            // may hand the caller a copy rather than the very same instance
            val thrown = thrownBy { repository.savePin(pin) }
            assertIs<IllegalStateException>(thrown)
            assertEquals(failure.message, thrown.message)
        }

    private inline fun thrownBy(block: () -> Unit): Throwable {
        try {
            block()
        } catch (error: Throwable) {
            return error
        }
        fail("Expected the call to fail")
    }
}
