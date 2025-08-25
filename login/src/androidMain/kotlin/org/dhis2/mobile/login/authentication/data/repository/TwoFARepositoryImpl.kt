package org.dhis2.mobile.login.authentication.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository
import org.hisp.dhis.android.core.D2

class TwoFARepositoryImpl(
    private val d2: D2,
) : TwoFARepository {
    override fun getTwoFAStatus(): Flow<TwoFAStatus> =
        flow {
            delay(2000) // Simulate network delay
            emit(TwoFAStatus.Enabled())
        }

    override fun disableTwoFAs(code: String): Flow<TwoFAStatus> =
        flow {
            d2.userModule().twoFactorAuthManager().disable2fa(code).fold(
                onSuccess = {
                    emit(TwoFAStatus.Disabled())
                },
                onFailure = {
                    emit(TwoFAStatus.Enabled(it.message ?: it.errorDescription()))
                },
            )
        }
}
