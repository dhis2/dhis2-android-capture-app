package org.dhis2.mobile.login.authentication.domain.repository

import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus

interface TwoFARepository {
    suspend fun getTwoFAStatus(): TwoFAStatus
    suspend fun enableTwoFA(code: String): Result<Unit>
    suspend fun disableTwoFAs(code: String): Result<Unit>
}
