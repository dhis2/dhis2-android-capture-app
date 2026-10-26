package org.dhis2.mobile.login.authentication.data.repository

import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository

/**
 * [TwoFARepository] for targets without the DHIS2 Android SDK: two-factor authentication cannot be
 * reached, so it reports no connection.
 */
internal class FakeTwoFARepository : TwoFARepository {
    override suspend fun getTwoFAStatus(): TwoFAStatus = TwoFAStatus.NoConnection

    override suspend fun enableTwoFA(
        code: String,
        isNetworkAvailable: Boolean,
    ): Result<Unit> = Result.failure(UnsupportedOperationException(NOT_SUPPORTED_MESSAGE))

    override suspend fun disableTwoFAs(
        code: String,
        isNetworkAvailable: Boolean,
    ): Result<Unit> = Result.failure(UnsupportedOperationException(NOT_SUPPORTED_MESSAGE))

    private companion object {
        const val NOT_SUPPORTED_MESSAGE = "Two-factor authentication is not available on this platform yet"
    }
}
