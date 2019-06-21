package org.dhis2.utils

import android.content.Context
import android.text.TextUtils
import org.dhis2.R
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.systeminfo.DHISVersion

class D2ErrorUtils {
    companion object {
        fun getErrorMessage(context: Context, throwable: Throwable): String {
            return if (throwable.cause is D2Error)
                handleD2Error(context, throwable.cause as D2Error)
            else (throwable as? D2Error)?.let { handleD2Error(context, it) }
                    ?: throwable.localizedMessage
        }

        private fun handleD2Error(context: Context, d2Error: D2Error): String {
            when (d2Error.errorCode()) {
                D2ErrorCode.UNEXPECTED -> return d2Error.errorDescription()
                D2ErrorCode.UNKNOWN_HOST -> return d2Error.errorDescription()
                D2ErrorCode.URL_NOT_FOUND -> return d2Error.errorDescription()
                D2ErrorCode.SOCKET_TIMEOUT -> return d2Error.errorDescription()
                D2ErrorCode.BAD_CREDENTIALS -> return d2Error.errorDescription()
                D2ErrorCode.ALREADY_EXECUTED -> return d2Error.errorDescription()
                D2ErrorCode.TOO_MANY_PERIODS -> return d2Error.errorDescription()
                D2ErrorCode.API_INVALID_QUERY -> return d2Error.errorDescription()
                D2ErrorCode.SEARCH_GRID_PARSE -> return d2Error.errorDescription()
                D2ErrorCode.NO_RESERVED_VALUES -> return d2Error.errorDescription()
                D2ErrorCode.TOO_MANY_ORG_UNITS -> return d2Error.errorDescription()
                D2ErrorCode.LOGIN_PASSWORD_NULL -> return context.getString(R.string.login_error_null_pass)
                D2ErrorCode.LOGIN_USERNAME_NULL -> return context.getString(R.string.login_error_null_username)
                D2ErrorCode.USER_ACCOUNT_LOCKED -> return d2Error.errorDescription()
                D2ErrorCode.INVALID_DHIS_VERSION -> return String.format(context.getString(R.string.login_error_dhis_version_v2), TextUtils.join(", ", DHISVersion.allowedVersionsAsStr()))
                D2ErrorCode.ALREADY_AUTHENTICATED -> return d2Error.errorDescription()
                D2ErrorCode.NO_AUTHENTICATED_USER -> return d2Error.errorDescription()
                D2ErrorCode.USER_ACCOUNT_DISABLED -> return d2Error.errorDescription()
                D2ErrorCode.OBJECT_CANT_BE_UPDATED -> return d2Error.errorDescription()
                D2ErrorCode.OWNERSHIP_ACCESS_DENIED -> return d2Error.errorDescription()
                D2ErrorCode.DIFFERENT_SERVER_OFFLINE -> return d2Error.errorDescription()
                D2ErrorCode.API_UNSUCCESSFUL_RESPONSE -> return context.getString(R.string.login_error_unsuccessful_response)
                D2ErrorCode.API_RESPONSE_PROCESS_ERROR -> return context.getString(R.string.login_error_error_response)
                D2ErrorCode.CANT_CREATE_EXISTING_OBJECT -> return d2Error.errorDescription()
                D2ErrorCode.NO_AUTHENTICATED_USER_OFFLINE -> return d2Error.errorDescription()
                D2ErrorCode.CANT_DELETE_NON_EXISTING_OBJECT -> return d2Error.errorDescription()
                D2ErrorCode.DIFFERENT_AUTHENTICATED_USER_OFFLINE -> return d2Error.errorDescription()
                else -> return d2Error.errorDescription()
            }
        }
    }
}