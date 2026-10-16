package org.dhis2.mobile.commons.error

import org.hisp.dhis.android.core.maintenance.D2Error

private const val MAX_CAUSE_DEPTH = 5

/**
 * The SDK error behind a failure, if there is one.
 *
 * [D2Error] is a checked exception, so the blocking RxJava operators the SDK still exposes rewrap
 * it in a RuntimeException before it reaches the data layer. Looking only at the failure itself
 * would miss every error those operators report.
 */
fun Throwable.asD2Error(): D2Error? {
    var current: Throwable? = this
    var depth = 0
    while (current != null && depth < MAX_CAUSE_DEPTH) {
        if (current is D2Error) return current
        if (current == current.cause) return null
        current = current.cause
        depth++
    }
    return null
}

/**
 * Runs an SDK call, reporting a [D2Error] failure as the mapped [DomainError] so nothing above the
 * data layer has to know about the SDK error type. Any other failure travels on untouched.
 */
suspend fun <T> DomainErrorMapper.withDomainErrors(block: suspend () -> T): T =
    try {
        block()
    } catch (error: Exception) {
        throw error.asD2Error()?.let { mapToDomainError(it) } ?: error
    }

/**
 * As [withDomainErrors], for a call that reports its failures as a [Result] instead of throwing.
 */
suspend fun <T> DomainErrorMapper.withDomainErrorsAsResult(block: suspend () -> Result<T>): Result<T> =
    try {
        block()
    } catch (error: Exception) {
        val d2Error = error.asD2Error() ?: throw error
        Result.failure(mapToDomainError(d2Error))
    }
