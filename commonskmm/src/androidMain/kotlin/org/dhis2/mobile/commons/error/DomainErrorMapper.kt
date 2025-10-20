package org.dhis2.mobile.commons.error

import kotlinx.coroutines.flow.firstOrNull
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.commons.resources.D2ErrorMessageProvider
import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.error_unexpected
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.jetbrains.compose.resources.getString

/**
 * Maps D2Error to DomainError
 * Just implemented in androidMain since D2Error belongs to Android SDK which is only available
 * in Android for now.
 */
class DomainErrorMapper(
    private val d2ErrorMessageProvider: D2ErrorMessageProvider,
    private val networkStatusProvider: NetworkStatusProvider,
) {
    suspend fun mapToDomainError(d2Error: D2Error): DomainError {
        val isNetworkAvailable =
            networkStatusProvider.connectionStatus.firstOrNull() ?: false
        val errorMessage =
            d2ErrorMessageProvider.getErrorMessage(
                d2Error,
                isNetworkAvailable,
            ) ?: getString(Res.string.error_unexpected)

        return when (d2Error.errorCode()) {
            D2ErrorCode.SERVER_CONNECTION_ERROR -> DomainError.ServerError(errorMessage)
            else -> DomainError.UnknownError(errorMessage)
        }
    }
}
