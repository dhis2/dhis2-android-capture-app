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
        else if (throwable instanceof D2Error)
            return handleD2Error(context,(D2Error)throwable);
        else
            return throwable.getLocalizedMessage();
    }

    private static String handleD2Error(Context context, D2Error d2Error) {
        switch (d2Error.errorCode()) {
            case LOGIN_PASSWORD_NULL:
                return context.getString(R.string.login_error_null_pass);
            case LOGIN_USERNAME_NULL:
                return context.getString(R.string.login_error_null_username);
            case INVALID_DHIS_VERSION:
                return String.format(context.getString(R.string.login_error_dhis_version_v2), TextUtils.join(", ", DHISVersion.allowedVersionsAsStr()));
            case API_UNSUCCESSFUL_RESPONSE:
                return context.getString(R.string.login_error_unsuccessful_response);
            case API_RESPONSE_PROCESS_ERROR:
                return context.getString(R.string.login_error_error_response);
            case UNEXPECTED:
            case UNKNOWN_HOST:
            case URL_NOT_FOUND:
            case SOCKET_TIMEOUT:
            case BAD_CREDENTIALS:
            case ALREADY_EXECUTED:
            case TOO_MANY_PERIODS:
            case API_INVALID_QUERY:
            case SEARCH_GRID_PARSE:
            case NO_RESERVED_VALUES:
            case TOO_MANY_ORG_UNITS:
            case USER_ACCOUNT_LOCKED:
            case ALREADY_AUTHENTICATED:
            case NO_AUTHENTICATED_USER:
            case USER_ACCOUNT_DISABLED:
            case OBJECT_CANT_BE_UPDATED:
            case OWNERSHIP_ACCESS_DENIED:
            case CANT_CREATE_EXISTING_OBJECT:
            case NO_AUTHENTICATED_USER_OFFLINE:
            case CANT_DELETE_NON_EXISTING_OBJECT:
            case DIFFERENT_AUTHENTICATED_USER_OFFLINE:
            default:
                return d2Error.errorDescription();

        }
    }

}
