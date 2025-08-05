package org.dhis2.mobile.login.authentication.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository

class TwoFARepositoryImpl : TwoFARepository {

    override fun getTwoFAStatus(): Flow<TwoFAStatus> = flow {
        delay(2000) // Simulate network delay

        emit(TwoFAStatus.NoConnection)
    }
}
