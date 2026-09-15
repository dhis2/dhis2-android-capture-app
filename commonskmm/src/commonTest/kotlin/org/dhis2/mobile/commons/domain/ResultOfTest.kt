package org.dhis2.mobile.commons.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.error.DomainError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ResultOfTest {
    @Test
    fun `GIVEN a body that returns a value WHEN it is run THEN the value is reported as a success`() =
        runTest {
            // WHEN
            val result = resultOf { "1234" }

            // THEN
            assertTrue(result.isSuccess)
            assertEquals("1234", result.getOrNull())
        }

    @Test
    fun `GIVEN a body that throws a DomainError WHEN it is run THEN the same error is reported as a failure`() =
        runTest {
            // GIVEN - DomainError extends Throwable, so catch (e: Exception) would let it escape
            val error = DomainError.DatabaseError("The PIN could not be saved")

            // WHEN
            val result = resultOf { throw error }

            // THEN - the same instance, so the caller can still tell the errors apart
            assertTrue(result.isFailure)
            assertSame(error, result.exceptionOrNull())
        }

    @Test
    fun `GIVEN a body that throws an Exception WHEN it is run THEN the same error is reported as a failure`() =
        runTest {
            // GIVEN
            val error = IllegalStateException("not a domain error")

            // WHEN
            val result = resultOf { throw error }

            // THEN
            assertTrue(result.isFailure)
            assertSame(error, result.exceptionOrNull())
        }

    @Test
    fun `GIVEN a body that is cancelled WHEN it is run THEN the cancellation propagates`() =
        runTest {
            // GIVEN - cancellation is not a failure, so it must not be swallowed into a Result
            val cancellation = CancellationException("the screen was closed")

            // WHEN & THEN
            val thrown =
                assertFailsWith<CancellationException> { resultOf { throw cancellation } }
            assertSame(cancellation, thrown)
        }
}
