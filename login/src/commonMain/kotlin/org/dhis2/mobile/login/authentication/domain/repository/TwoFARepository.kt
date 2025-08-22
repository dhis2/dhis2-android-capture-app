package org.dhis2.mobile.login.authentication.domain.repository

import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus

interface TwoFARepository {
    fun getTwoFAStatus(): Flow<TwoFAStatus>

    fun disableTwoFAs(code: String): Flow<TwoFAStatus>
}
