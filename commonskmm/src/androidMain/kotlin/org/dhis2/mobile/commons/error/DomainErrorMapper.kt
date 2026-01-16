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
 * Maps D2Error from DHIS2 Android SDK to DomainError for the application domain layer.
 * Categorizes errors by their nature and typical handling strategy.
 *
 * Note: Only implemented in androidMain since D2Error belongs to the Android SDK,
 * which is only available in Android for now.
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
            // Authentication errors
            D2ErrorCode.LOGIN_PASSWORD_NULL,
            D2ErrorCode.LOGIN_USERNAME_NULL,
            D2ErrorCode.BAD_CREDENTIALS,
            D2ErrorCode.NO_AUTHENTICATED_USER,
            D2ErrorCode.NO_AUTHENTICATED_USER_OFFLINE,
            D2ErrorCode.DIFFERENT_AUTHENTICATED_USER_OFFLINE,
            D2ErrorCode.ALREADY_AUTHENTICATED,
            -> DomainError.AuthenticationError(errorMessage)

            // User account issues (disabled, locked)
            D2ErrorCode.USER_ACCOUNT_DISABLED,
            D2ErrorCode.USER_ACCOUNT_LOCKED,
            -> DomainError.PermissionDeniedError(errorMessage)

            // User lacks authorization for program/operation access
            D2ErrorCode.PROGRAM_ACCESS_CLOSED,
            -> DomainError.UnauthorizedAccessError(errorMessage)

            // Ownership-based access denied
            D2ErrorCode.OWNERSHIP_ACCESS_DENIED,
            -> DomainError.PermissionDeniedError(errorMessage)

            // Network connectivity errors
            D2ErrorCode.SOCKET_TIMEOUT,
            D2ErrorCode.UNKNOWN_HOST,
            D2ErrorCode.URL_NOT_FOUND,
            D2ErrorCode.SSL_ERROR,
            D2ErrorCode.TOO_MANY_REQUESTS,
            -> DomainError.NetworkError(errorMessage)

            // Server errors
            D2ErrorCode.SERVER_CONNECTION_ERROR,
            ->
                when {
                    isNetworkAvailable -> DomainError.ServerError(errorMessage)
                    else -> DomainError.NetworkError(errorMessage)
                }

            // API/Request errors
            D2ErrorCode.API_UNSUCCESSFUL_RESPONSE,
            D2ErrorCode.API_RESPONSE_PROCESS_ERROR,
            D2ErrorCode.API_INVALID_QUERY,
            D2ErrorCode.SEARCH_GRID_PARSE,
            -> DomainError.ApiError(errorMessage)

            // Server configuration/URL errors
            D2ErrorCode.NO_DHIS2_SERVER,
            D2ErrorCode.SERVER_URL_NULL,
            D2ErrorCode.SERVER_URL_MALFORMED,
            D2ErrorCode.INVALID_DHIS_VERSION,
            -> DomainError.ConfigurationError(errorMessage)

            // Database errors
            D2ErrorCode.DATABASE_EXPORT_LOGIN_FIRST,
            D2ErrorCode.DATABASE_EXPORT_ENCRYPTED_NOT_SUPPORTED,
            D2ErrorCode.DATABASE_IMPORT_ALREADY_EXISTS,
            D2ErrorCode.DATABASE_IMPORT_LOGOUT_FIRST,
            D2ErrorCode.DATABASE_IMPORT_VERSION_HIGHER_THAN_SUPPORTED,
            D2ErrorCode.DATABASE_IMPORT_FAILED,
            D2ErrorCode.DATABASE_IMPORT_INVALID_FILE,
            -> DomainError.DatabaseError(errorMessage)

            // Data validation errors
            D2ErrorCode.INVALID_CHARACTERS,
            D2ErrorCode.INVALID_GEOMETRY_VALUE,
            D2ErrorCode.CANT_CREATE_EXISTING_OBJECT,
            D2ErrorCode.CANT_DELETE_NON_EXISTING_OBJECT,
            D2ErrorCode.OBJECT_CANT_BE_INSERTED,
            D2ErrorCode.OBJECT_CANT_BE_UPDATED,
            D2ErrorCode.VALUE_CANT_BE_SET,
            D2ErrorCode.RELATIONSHIPS_CANT_BE_UPDATED,
            D2ErrorCode.MAX_TEI_COUNT_REACHED,
            D2ErrorCode.TOO_MANY_ORG_UNITS,
            D2ErrorCode.TOO_MANY_PERIODS,
            D2ErrorCode.MIN_SEARCH_ATTRIBUTES_REQUIRED,
            D2ErrorCode.ORGUNIT_NOT_IN_SEARCH_SCOPE,
            -> DomainError.DataValidationError(errorMessage)

            // Data not found errors
            D2ErrorCode.NO_RESERVED_VALUES,
            D2ErrorCode.FILE_NOT_FOUND,
            -> DomainError.DataNotFoundError(errorMessage)

            // App configuration errors
            D2ErrorCode.APP_NAME_NOT_SET,
            D2ErrorCode.APP_VERSION_NOT_SET,
            D2ErrorCode.SETTINGS_APP_NOT_SUPPORTED,
            D2ErrorCode.SETTINGS_APP_NOT_INSTALLED,
            D2ErrorCode.CANT_ACCESS_KEYSTORE,
            D2ErrorCode.CANT_INSTANTIATE_KEYSTORE,
            -> DomainError.ConfigurationError(errorMessage)

            // Reserved values/quota errors - use DataValidationError
            D2ErrorCode.COULD_NOT_RESERVE_VALUE_ON_SERVER,
            D2ErrorCode.NOT_ENOUGH_VALUES_LEFT_TO_RESERVE_ON_SERVER,
            D2ErrorCode.MIGHT_BE_RUNNING_LOW_ON_AVAILABLE_VALUES,
            D2ErrorCode.VALUES_RESERVATION_TOOK_TOO_LONG,
            -> DomainError.DataValidationError(errorMessage)

            // Uncommon/rare errors - use UnexpectedError
            D2ErrorCode.UNEXPECTED,
            D2ErrorCode.JOB_REPORT_NOT_AVAILABLE,
            D2ErrorCode.FAIL_RESIZING_IMAGE,
            D2ErrorCode.IMPOSSIBLE_TO_GENERATE_COORDINATES,
            D2ErrorCode.SMS_NOT_SUPPORTED,
            D2ErrorCode.ALREADY_EXECUTED,
            D2ErrorCode.NOT_IN_TOTP_2FA_ENROLLMENT_MODE,
            -> DomainError.UnexpectedError(errorMessage)
        }
    }
}
