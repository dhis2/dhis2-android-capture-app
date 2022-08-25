package org.dhis2.android.rtsm.utils

import android.content.Context
import org.dhis2.android.rtsm.BuildConfig
import org.dhis2.android.rtsm.R
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode

class Sdk {
    companion object {
        @JvmStatic
        fun d2(context: Context): D2 {
            return try {
                D2Manager.getD2()
            } catch (e: IllegalStateException) {
                D2Manager.blockingInstantiateD2(getD2Configuration(context))!!
            }
        }

        fun getD2Configuration(context: Context): D2Configuration {
            // This will be null if not debug mode to make sure your data is safe

            return D2Configuration.builder()
                .appName(BuildConfig.APPLICATION_ID)
                .appVersion(BuildConfig.VERSION_NAME)
                .readTimeoutInSeconds(10 * 60)
                .connectTimeoutInSeconds(10 * 60)
                .writeTimeoutInSeconds(10 * 60)
                .context(context)
                .build()
        }

        fun getFriendlyErrorMessage(throwable: Throwable): Int? {
            return when {
                throwable.cause is D2Error -> processD2Error(throwable.cause as D2Error)
                throwable is D2Error -> processD2Error(throwable)
                else -> null
            }
        }

        private fun processD2Error(d2Error: D2Error): Int? {
            return when (d2Error.errorCode()) {
                D2ErrorCode.API_UNSUCCESSFUL_RESPONSE -> R.string.login_error_unsuccessful_response
                D2ErrorCode.API_RESPONSE_PROCESS_ERROR -> R.string.login_error_error_response
                D2ErrorCode.NO_DHIS2_SERVER -> R.string.login_error_no_dhis_instance
                D2ErrorCode.BAD_CREDENTIALS -> R.string.login_error_bad_credentials
                D2ErrorCode.UNKNOWN_HOST -> R.string.login_error_unknown_host
                D2ErrorCode.UNEXPECTED -> R.string.error_unexpected
                D2ErrorCode.TOO_MANY_REQUESTS -> R.string.error_many_requests
                D2ErrorCode.ALREADY_AUTHENTICATED -> R.string.error_already_authenticated
                D2ErrorCode.ALREADY_EXECUTED -> R.string.error_already_executed
                D2ErrorCode.API_INVALID_QUERY -> R.string.error_api_invalid_query
                D2ErrorCode.CANT_CREATE_EXISTING_OBJECT -> R.string.error_create_existing_error
                D2ErrorCode.DATABASE_EXPORT_LOGIN_FIRST -> R.string.error_export_login
                D2ErrorCode.DATABASE_EXPORT_ENCRYPTED_NOT_SUPPORTED -> R.string.error_export_encrypted
                D2ErrorCode.DATABASE_IMPORT_ALREADY_EXISTS -> R.string.error_import_exist
                D2ErrorCode.DATABASE_IMPORT_LOGOUT_FIRST -> R.string.error_import_logout
                D2ErrorCode.DATABASE_IMPORT_VERSION_HIGHER_THAN_SUPPORTED -> R.string.error_import_version
                D2ErrorCode.NO_AUTHENTICATED_USER -> R.string.error_user_no_authenticated
                D2ErrorCode.NO_AUTHENTICATED_USER_OFFLINE -> R.string.error_user_no_authenticated_offline
                D2ErrorCode.DIFFERENT_AUTHENTICATED_USER_OFFLINE -> R.string.error_different_offline_user
                D2ErrorCode.OBJECT_CANT_BE_UPDATED -> R.string.error_object_update
                D2ErrorCode.OBJECT_CANT_BE_INSERTED -> R.string.error_object_insert
                D2ErrorCode.OWNERSHIP_ACCESS_DENIED -> R.string.error_ownership_access
                D2ErrorCode.SEARCH_GRID_PARSE -> R.string.online_search_parsing_error
                D2ErrorCode.SERVER_URL_NULL -> R.string.error_null_url
                D2ErrorCode.SERVER_URL_MALFORMED -> R.string.error_server_malformed
                D2ErrorCode.SETTINGS_APP_NOT_SUPPORTED -> R.string.error_settings_app_not_supported
                D2ErrorCode.SETTINGS_APP_NOT_INSTALLED -> R.string.error_settings_app_not_intalled
                D2ErrorCode.SOCKET_TIMEOUT -> R.string.error_socket_timeout
                D2ErrorCode.URL_NOT_FOUND -> R.string.error_url_not_found
                D2ErrorCode.USER_ACCOUNT_DISABLED -> R.string.error_account_disabled
                D2ErrorCode.USER_ACCOUNT_LOCKED -> R.string.error_account_locked
                D2ErrorCode.VALUE_CANT_BE_SET -> R.string.error_set_value
                D2ErrorCode.SSL_ERROR -> R.string.error_ssl
                else -> null
            }
        }
    }
}