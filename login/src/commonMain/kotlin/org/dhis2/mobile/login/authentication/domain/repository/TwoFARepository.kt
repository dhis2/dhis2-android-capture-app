package org.dhis2.mobile.login.authentication.domain.repository

import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus

interface TwoFARepository {
    suspend fun getTwoFAStatus(): TwoFAStatus

    suspend fun disableTwoFAs(code: String): Flow<TwoFAStatus>
}
