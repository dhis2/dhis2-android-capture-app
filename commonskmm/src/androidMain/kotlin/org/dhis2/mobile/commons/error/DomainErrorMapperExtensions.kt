package org.dhis2.mobile.commons.error

import org.hisp.dhis.android.core.maintenance.D2Error

/**
 * Runs an SDK call, reporting a [D2Error] failure as the mapped [DomainError] so nothing above the
 * data layer has to know about the SDK error type.
 */
suspend fun <T> DomainErrorMapper.withDomainErrors(block: suspend () -> T): T =
    try {
        block()
    } catch (d2Error: D2Error) {
        throw mapToDomainError(d2Error)
    }

/**
 * As [withDomainErrors], for a call that reports its failures as a [Result] instead of throwing.
 */
suspend fun <T> DomainErrorMapper.withDomainErrorsAsResult(block: suspend () -> Result<T>): Result<T> =
    try {
        block()
    } catch (d2Error: D2Error) {
        Result.failure(mapToDomainError(d2Error))
    }
