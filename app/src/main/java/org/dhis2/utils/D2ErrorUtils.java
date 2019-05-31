package org.dhis2.utils;

import android.content.Context;
import android.text.TextUtils;

import org.dhis2.R;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.systeminfo.DHISVersion;

public class D2ErrorUtils {

    public static String getErrorMessage(Context context, Throwable throwable) {
        if (throwable.getCause() instanceof D2Error)
            return handleD2Error(context, (D2Error) throwable.getCause());
        else
            return throwable.getLocalizedMessage();
    }

    private static String handleD2Error(Context context, D2Error d2Error) {
        switch (d2Error.errorCode()) {
            case UNEXPECTED:
                return d2Error.errorDescription();
            case UNKNOWN_HOST:
                return d2Error.errorDescription();
            case URL_NOT_FOUND:
                return d2Error.errorDescription();
            case SOCKET_TIMEOUT:
                return d2Error.errorDescription();
            case BAD_CREDENTIALS:
                return d2Error.errorDescription();
            case ALREADY_EXECUTED:
                return d2Error.errorDescription();
            case TOO_MANY_PERIODS:
                return d2Error.errorDescription();
            case API_INVALID_QUERY:
                return d2Error.errorDescription();
            case SEARCH_GRID_PARSE:
                return d2Error.errorDescription();
            case NO_RESERVED_VALUES:
                return d2Error.errorDescription();
            case TOO_MANY_ORG_UNITS:
                return d2Error.errorDescription();
            case LOGIN_PASSWORD_NULL:
                return context.getString(R.string.login_error_null_pass);
            case LOGIN_USERNAME_NULL:
                return context.getString(R.string.login_error_null_username);
            case USER_ACCOUNT_LOCKED:
                return d2Error.errorDescription();
            case INVALID_DHIS_VERSION:
                return String.format(context.getString(R.string.login_error_dhis_version_v2), TextUtils.join(", ", DHISVersion.allowedVersionsAsStr()));
            case ALREADY_AUTHENTICATED:
                return d2Error.errorDescription();
            case NO_AUTHENTICATED_USER:
                return d2Error.errorDescription();
            case USER_ACCOUNT_DISABLED:
                return d2Error.errorDescription();
            case OBJECT_CANT_BE_UPDATED:
                return d2Error.errorDescription();
            case OWNERSHIP_ACCESS_DENIED:
                return d2Error.errorDescription();
            case DIFFERENT_SERVER_OFFLINE:
                return d2Error.errorDescription();
            case API_UNSUCCESSFUL_RESPONSE:
                return context.getString(R.string.login_error_unsuccessful_response);
            case API_RESPONSE_PROCESS_ERROR:
                return context.getString(R.string.login_error_error_response);
            case CANT_CREATE_EXISTING_OBJECT:
                return d2Error.errorDescription();
            case NO_AUTHENTICATED_USER_OFFLINE:
                return d2Error.errorDescription();
            case CANT_DELETE_NON_EXISTING_OBJECT:
                return d2Error.errorDescription();
            case DIFFERENT_AUTHENTICATED_USER_OFFLINE:
                return d2Error.errorDescription();
            default:
                return d2Error.errorDescription();

        }
    }

}
