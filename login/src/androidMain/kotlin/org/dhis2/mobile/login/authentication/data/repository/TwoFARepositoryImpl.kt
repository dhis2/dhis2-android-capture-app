package org.dhis2.mobile.login.authentication.data.repository

import kotlinx.coroutines.delay
import org.dhis2.mobile.commons.resources.D2ErrorMessageProvider
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.arch.helpers.Result as Dhis2Result

private const val TWO_FA_FEATURE_FLAG = "TWO_FACTOR_AUTHENTICATION"
private var isEnabledForTesting = false

class TwoFARepositoryImpl(
    private val d2: D2,
    private val d2ErrorMessageProvider: D2ErrorMessageProvider,
) : TwoFARepository {
    override suspend fun getTwoFAStatus(): TwoFAStatus {
        val is2FAEnabled =
            if (featureFlagEnabled()) {
                isEnabledForTesting
            } else {
                d2.userModule().twoFactorAuthManager().is2faEnabled()
            }

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
    ): Result<Unit> {
        delay(3000) // TODO: Delete line when feature flag is removed
        val result =
            if (featureFlagEnabled()) {
                isEnabledForTesting = true
                Dhis2Result.Success(Unit)
            } else {
                d2.userModule().twoFactorAuthManager().enable2fa(code)
            }
        return when (result) {
            is Dhis2Result.Failure ->
                Result.failure(
                    Exception(
                        d2ErrorMessageProvider.getErrorMessage(result.failure, isNetworkAvailable),
                    ),
                )

            is Dhis2Result.Success ->
                Result.success(Unit)
        }
    }

    override suspend fun disableTwoFAs(
        code: String,
        isNetworkAvailable: Boolean,
    ): Result<Unit> {
        delay(3000) // TODO: Delete line when feature flag is removed
        val result =
            if (featureFlagEnabled()) {
                isEnabledForTesting = false
                Dhis2Result.Success(Unit)
            } else {
                d2.userModule().twoFactorAuthManager().disable2fa(code)
            }
        return when (result) {
            is Dhis2Result.Failure ->
                Result.failure(
                    Exception(
                        d2ErrorMessageProvider.getErrorMessage(result.failure, isNetworkAvailable),
                    ),
                )

            is Dhis2Result.Success -> Result.success(Unit)
        }
    }

    private fun featureFlagEnabled() =
        d2
            .dataStoreModule()
            .localDataStore()
            .value(TWO_FA_FEATURE_FLAG)
            .blockingGet()
            ?.value()
            ?.toBooleanStrictOrNull() == true
}
