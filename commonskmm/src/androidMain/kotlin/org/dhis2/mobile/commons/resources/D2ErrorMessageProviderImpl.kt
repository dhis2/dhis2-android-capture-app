package org.dhis2.mobile.commons.resources

import android.text.TextUtils
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.systeminfo.DHISVersion
import org.jetbrains.compose.resources.getString

class D2ErrorMessageProviderImpl : D2ErrorMessageProvider {
    override suspend fun getErrorMessage(
        throwable: Throwable,
        isNetworkAvailable: Boolean,
    ): String? =
        when {
            throwable.cause is D2Error ->
                handleD2Error(
                    throwable.cause as D2Error,
                    isNetworkAvailable,
                )

            throwable is D2Error -> handleD2Error(throwable, isNetworkAvailable)
            else -> throwable.localizedMessage
        }

    private suspend fun handleD2Error(
        d2Error: D2Error?,
        isNetworkAvailable: Boolean,
    ): String =
        when (d2Error!!.errorCode()) {
            D2ErrorCode.LOGIN_PASSWORD_NULL ->
                getString(Res.string.login_error_null_pass)

            D2ErrorCode.LOGIN_USERNAME_NULL ->
                getString(Res.string.login_error_null_username)

            D2ErrorCode.INVALID_DHIS_VERSION ->
                String.format(
                    getString(Res.string.login_error_dhis_version_v2),
                    TextUtils.join(", ", DHISVersion.allowedVersionsAsStr()),
                )

            D2ErrorCode.API_UNSUCCESSFUL_RESPONSE ->
                getString(Res.string.login_error_unsuccessful_response)

            D2ErrorCode.API_RESPONSE_PROCESS_ERROR ->
                getString(Res.string.login_error_error_response)

            D2ErrorCode.NO_DHIS2_SERVER ->
                getString(Res.string.login_error_no_dhis_instance)

            D2ErrorCode.BAD_CREDENTIALS ->
                getString(Res.string.login_error_bad_credentials)

            D2ErrorCode.UNKNOWN_HOST ->
                getString(Res.string.login_error_unknown_host)

            D2ErrorCode.UNEXPECTED ->
                getString(Res.string.error_unexpected)

            D2ErrorCode.TOO_MANY_ORG_UNITS ->
                getString(Res.string.error_too_manu_org_units)

            D2ErrorCode.MAX_TEI_COUNT_REACHED ->
                getString(Res.string.error_max_tei_count_reached)

            D2ErrorCode.TOO_MANY_REQUESTS ->
                getString(Res.string.error_many_requests)

            D2ErrorCode.ALREADY_AUTHENTICATED ->
                getString(Res.string.error_already_authenticated)

            D2ErrorCode.ALREADY_EXECUTED ->
                getString(Res.string.error_already_executed)

            D2ErrorCode.API_INVALID_QUERY ->
                getString(Res.string.error_api_invalid_query)

            D2ErrorCode.APP_NAME_NOT_SET ->
                getString(Res.string.error_app_name_not_set)

            D2ErrorCode.APP_VERSION_NOT_SET ->
                getString(Res.string.error_app_version_not_set)

            D2ErrorCode.CANT_ACCESS_KEYSTORE ->
                getString(Res.string.error_access_keystore)

            D2ErrorCode.CANT_CREATE_EXISTING_OBJECT ->
                getString(Res.string.error_create_existing_error)

            D2ErrorCode.CANT_DELETE_NON_EXISTING_OBJECT ->
                getString(Res.string.error_delete_non_existing_object)

            D2ErrorCode.CANT_INSTANTIATE_KEYSTORE ->
                getString(Res.string.error_instance_keystore)

            D2ErrorCode.COULD_NOT_RESERVE_VALUE_ON_SERVER ->
                getString(Res.string.error_reserve_value_on_server)

            D2ErrorCode.DATABASE_EXPORT_LOGIN_FIRST ->
                getString(Res.string.error_export_login)

            D2ErrorCode.DATABASE_EXPORT_ENCRYPTED_NOT_SUPPORTED ->
                getString(Res.string.error_export_encrypted)

            D2ErrorCode.DATABASE_IMPORT_ALREADY_EXISTS ->
                getString(Res.string.error_import_exist)

            D2ErrorCode.DATABASE_IMPORT_LOGOUT_FIRST ->
                getString(Res.string.error_import_logout)

            D2ErrorCode.DATABASE_IMPORT_VERSION_HIGHER_THAN_SUPPORTED ->
                getString(Res.string.error_import_version)

            D2ErrorCode.FILE_NOT_FOUND ->
                getString(Res.string.error_file_not_found)

            D2ErrorCode.FAIL_RESIZING_IMAGE ->
                getString(Res.string.error_file_resize)

            D2ErrorCode.IMPOSSIBLE_TO_GENERATE_COORDINATES ->
                getString(Res.string.error_generate_coordinate)

            D2ErrorCode.JOB_REPORT_NOT_AVAILABLE ->
                getString(Res.string.error_job)

            D2ErrorCode.MIGHT_BE_RUNNING_LOW_ON_AVAILABLE_VALUES ->
                getString(Res.string.error_low_on_available_values)

            D2ErrorCode.NO_AUTHENTICATED_USER ->
                getString(Res.string.error_user_no_authenticated)

            D2ErrorCode.NO_AUTHENTICATED_USER_OFFLINE ->
                getString(Res.string.error_user_no_authenticated_offline)

            D2ErrorCode.NOT_ENOUGH_VALUES_LEFT_TO_RESERVE_ON_SERVER ->
                getString(Res.string.error_no_values_left_on_server)

            D2ErrorCode.DIFFERENT_AUTHENTICATED_USER_OFFLINE ->
                getString(Res.string.error_different_offline_user)

            D2ErrorCode.INVALID_GEOMETRY_VALUE ->
                getString(Res.string.error_invalid_geometry)

            D2ErrorCode.NO_RESERVED_VALUES ->
                getString(Res.string.error_no_reserved_values)

            D2ErrorCode.OBJECT_CANT_BE_UPDATED ->
                getString(Res.string.error_object_update)

            D2ErrorCode.OBJECT_CANT_BE_INSERTED ->
                getString(Res.string.error_object_insert)

            D2ErrorCode.OWNERSHIP_ACCESS_DENIED ->
                getString(Res.string.error_ownership_access)

            D2ErrorCode.SEARCH_GRID_PARSE ->
                getString(Res.string.online_search_parsing_error)

            D2ErrorCode.SERVER_URL_NULL ->
                getString(Res.string.error_null_url)

            D2ErrorCode.SERVER_URL_MALFORMED ->
                getString(Res.string.error_server_malformed)

            D2ErrorCode.SETTINGS_APP_NOT_SUPPORTED ->
                getString(Res.string.error_settings_app_not_supported)

            D2ErrorCode.SETTINGS_APP_NOT_INSTALLED ->
                getString(Res.string.error_settings_app_not_intalled)

            D2ErrorCode.SOCKET_TIMEOUT ->
                getString(Res.string.error_socket_timeout)

            D2ErrorCode.RELATIONSHIPS_CANT_BE_UPDATED ->
                getString(Res.string.error_relationship_updated)

            D2ErrorCode.TOO_MANY_PERIODS ->
                getString(Res.string.error_too_many_periods)

            D2ErrorCode.URL_NOT_FOUND ->
                getString(Res.string.error_url_not_found)

            D2ErrorCode.USER_ACCOUNT_DISABLED ->
                getString(Res.string.error_account_disabled)

            D2ErrorCode.USER_ACCOUNT_LOCKED ->
                getString(Res.string.error_account_locked)

            D2ErrorCode.VALUE_CANT_BE_SET ->
                getString(Res.string.error_set_value)

            D2ErrorCode.VALUES_RESERVATION_TOOK_TOO_LONG ->
                getString(Res.string.error_value_reservation_time)

            D2ErrorCode.SSL_ERROR ->
                getString(Res.string.error_ssl)

            D2ErrorCode.SMS_NOT_SUPPORTED ->
                getString(Res.string.error_sms_not_supported)

            D2ErrorCode.MIN_SEARCH_ATTRIBUTES_REQUIRED ->
                getString(Res.string.error_min_attributes)

            D2ErrorCode.ORGUNIT_NOT_IN_SEARCH_SCOPE ->
                getString(Res.string.error_org_unit_scope)

            D2ErrorCode.INVALID_CHARACTERS ->
                getString(Res.string.error_invalid_characters)

            D2ErrorCode.PROGRAM_ACCESS_CLOSED -> defaultError()
            D2ErrorCode.SERVER_CONNECTION_ERROR ->
                if (isNetworkAvailable) {
                    getString(Res.string.error_server_unavailable)
                } else {
                    getString(Res.string.error_no_internet_connection)
                }

            D2ErrorCode.DATABASE_IMPORT_FAILED ->
                getString(Res.string.database_import_failed)

            D2ErrorCode.DATABASE_IMPORT_INVALID_FILE ->
                getString(Res.string.invalid_file)

            D2ErrorCode.NOT_IN_TOTP_2FA_ENROLLMENT_MODE ->
                getString(Res.string.not_in_totp_2fa_enrollment_mode)
        }

    private suspend fun defaultError() = getString(Res.string.error_unexpected)
}
