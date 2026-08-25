package org.dhis2.mobile.commons.error

import kotlinx.coroutines.test.runTest
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

class DomainErrorMapperExtensionsTest {
    private val mapper: DomainErrorMapper = mock()

    private val d2Error =
        D2Error
            .builder()
            .errorCode(D2ErrorCode.OAUTH2_NO_VALID_TOKEN)
            .errorDescription("There is no access token")
            .build()

    private val mappedError = DomainError.SessionRenewalRequiredError("Your session has expired")

    @Test
    fun `GIVEN an SDK error WHEN a call reports it by throwing THEN the domain error is thrown`() =
        runTest {
            // GIVEN
            whenever(mapper.mapToDomainError(d2Error)) doReturn mappedError

            // WHEN & THEN
            assertEquals(mappedError, thrownBy { mapper.withDomainErrors { throw d2Error } })
        }

    @Test
    fun `GIVEN an SDK error wrapped by RxJava WHEN a call reports it THEN the domain error is thrown`() =
        runTest {
            // GIVEN - D2Error is a checked exception, so the blocking RxJava operators the SDK
            // exposes rewrap it in a RuntimeException before it reaches the data layer
            whenever(mapper.mapToDomainError(d2Error)) doReturn mappedError

            // WHEN & THEN - missing this is missing every error those operators report
            assertEquals(
                mappedError,
                thrownBy { mapper.withDomainErrors { throw RuntimeException(d2Error) } },
            )
        }

    @Test
    fun `GIVEN an unrelated failure WHEN a call reports it THEN it travels on untouched`() =
        runTest {
            // GIVEN - nothing to map, so nothing should be invented
            val failure = IllegalStateException("not an SDK error")

            // WHEN & THEN
            assertIs<IllegalStateException>(thrownBy { mapper.withDomainErrors { throw failure } })
        }

    @Test
    fun `GIVEN an SDK error wrapped by RxJava WHEN a call reports results THEN a failed result carries the domain error`() =
        runTest {
            // GIVEN
            whenever(mapper.mapToDomainError(d2Error)) doReturn mappedError

            // WHEN
            val result =
                mapper.withDomainErrorsAsResult<Unit> { throw RuntimeException(d2Error) }

            // THEN
            assertEquals(mappedError, result.exceptionOrNull())
        }

    @Test
    fun `GIVEN an unrelated failure WHEN a call reports results THEN it travels on untouched`() =
        runTest {
            // GIVEN
            val failure = IllegalStateException("not an SDK error")

            // WHEN & THEN
            assertIs<IllegalStateException>(
                thrownBy { mapper.withDomainErrorsAsResult<Unit> { throw failure } },
            )
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
