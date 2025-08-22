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
    override suspend fun getTwoFAStatus(): TwoFAStatus =
        if (d2.userModule().twoFactorAuthManager().is2faEnabled()) {
            TwoFAStatus.Enabled()
        } else {
            // TwoFAStatus.Disabled()
            // return Enabled just for test disable screen
            TwoFAStatus.Disabled()
        }

    override fun getTwoFASecretCode(): Flow<String> =
        flow {
            try {
                emit(d2.userModule().twoFactorAuthManager().getTotpSecret())
            } catch (e: Exception) {
                emit("")
            }
        }

    override fun enableTwoFA(code: String): Flow<Boolean> =
        flow {
            d2.userModule().twoFactorAuthManager().enable2fa(code).fold(
                onSuccess = {
                    emit(true)
                },
                onFailure = {
                    emit(false)
                },
            )
        }

    override suspend fun disableTwoFAs(code: String): Flow<TwoFAStatus> =
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
