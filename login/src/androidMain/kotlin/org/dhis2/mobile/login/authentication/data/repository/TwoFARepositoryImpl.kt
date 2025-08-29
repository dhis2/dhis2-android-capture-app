package org.dhis2.mobile.login.authentication.data.repository

import kotlinx.coroutines.delay
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.Result as Dhis2Result

private const val TWO_FA_FEATURE_FLAG = "TWO_FACTOR_AUTHENTICATION"
private var isEnabledForTesting = false

class TwoFARepositoryImpl(
    private val d2: D2,
) : TwoFARepository {
    override suspend fun getTwoFAStatus(): TwoFAStatus {
        val is2FAEnabled = if (featureFlagEnabled()) {
            isEnabledForTesting
        } else {
            d2.userModule().twoFactorAuthManager().is2faEnabled()
        }

        return if (is2FAEnabled) {
            TwoFAStatus.Enabled()
        } else {
            TwoFAStatus.Disabled(
                secretCode = d2.userModule().twoFactorAuthManager().getTotpSecret(),
            )
        }
    }

    override suspend fun enableTwoFA(code: String): Result<Unit> {
        delay(3000)
        val result = if (featureFlagEnabled()) {
            isEnabledForTesting = true
            Dhis2Result.Success(Unit)
        } else {
            d2.userModule().twoFactorAuthManager().enable2fa(code)
        }
        return when (result) {
            is Dhis2Result.Failure ->
                Result.failure(
                    Exception(
                        result.failure.message ?: result.failure.errorDescription(),
                    ),
                )

            is Dhis2Result.Success ->
                Result.success(Unit)
        }
    }

    override suspend fun disableTwoFAs(code: String): Result<Unit> {
        delay(3000)
        val result = if (featureFlagEnabled()) {
            isEnabledForTesting = false
            Dhis2Result.Success(Unit)
        } else {
            d2.userModule().twoFactorAuthManager().disable2fa(code)
        }
        return when (result) {
            is Dhis2Result.Failure ->
                Result.failure(
                    Exception(
                        result.failure.message ?: result.failure.errorDescription(),
                    ),
                )

            is Dhis2Result.Success -> Result.success(Unit)
        }
    }

    private fun featureFlagEnabled() = d2.dataStoreModule().localDataStore()
        .value(TWO_FA_FEATURE_FLAG)
        .blockingGet()?.value()?.toBooleanStrictOrNull() == true
}
