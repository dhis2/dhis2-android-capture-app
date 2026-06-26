package org.dhis2.mobile.login.authentication.data.repository

import org.dhis2.mobile.commons.resources.D2ErrorMessageProvider
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.arch.helpers.Result as Dhis2Result

class TwoFARepositoryImpl(
    private val d2: D2,
    private val d2ErrorMessageProvider: D2ErrorMessageProvider,
) : TwoFARepository {
    override suspend fun getTwoFAStatus(): TwoFAStatus {
        val is2FAEnabled = d2.userModule().twoFactorAuthManager().is2faEnabled()

        return if (is2FAEnabled) {
            TwoFAStatus.Enabled()
        } else {
            try {
                TwoFAStatus.Disabled(
                    secretCode = d2.userModule().twoFactorAuthManager().getTotpSecret(),
                )
            } catch (d2Error: D2Error) {
                TwoFAStatus.Enabled(
                    errorMessage = d2Error.localizedMessage ?: d2Error.errorDescription(),
                )
            }
        }
    }

    override suspend fun enableTwoFA(
        code: String,
        isNetworkAvailable: Boolean,
    ) = when (val result = d2.userModule().twoFactorAuthManager().enable2fa(code)) {
        is Dhis2Result.Failure ->
            Result.failure(
                Exception(
                    d2ErrorMessageProvider.getErrorMessage(result.failure, isNetworkAvailable),
                ),
            )

        is Dhis2Result.Success ->
            Result.success(Unit)
    }

    override suspend fun disableTwoFAs(
        code: String,
        isNetworkAvailable: Boolean,
    ) = when (val result = d2.userModule().twoFactorAuthManager().disable2fa(code)) {
        is Dhis2Result.Failure ->
            Result.failure(
                Exception(
                    d2ErrorMessageProvider.getErrorMessage(result.failure, isNetworkAvailable),
                ),
            )

        is Dhis2Result.Success -> Result.success(Unit)
    }
}
