package org.dhis2.mobile.commons.domain

import kotlinx.coroutines.CancellationException
import org.dhis2.mobile.commons.error.DomainError

/**
 * Runs a use case body, reporting any failure as [Result.failure] -- including a [DomainError],
 * which `catch (e: Exception)` does not catch because [DomainError] extends Throwable.
 *
 * Cancellation is not a failure, so it propagates instead of being reported as one. That is also
 * why the stdlib `runCatching` is not a substitute: it does catch [Throwable], but it swallows
 * [CancellationException] and so must not be used inside a coroutine.
 */
suspend fun <T> resultOf(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        Result.failure(error)
    }
