package org.dhis2.mobile.commons.error

import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.http_error_bad_gateway
import org.dhis2.mobile.commons.resources.http_error_bad_request
import org.dhis2.mobile.commons.resources.http_error_conflict
import org.dhis2.mobile.commons.resources.http_error_forbidden
import org.dhis2.mobile.commons.resources.http_error_gateway_timeout
import org.dhis2.mobile.commons.resources.http_error_internal_server_error
import org.dhis2.mobile.commons.resources.http_error_method_not_allowed
import org.dhis2.mobile.commons.resources.http_error_not_found
import org.dhis2.mobile.commons.resources.http_error_request_timeout
import org.dhis2.mobile.commons.resources.http_error_service_unavailable
import org.dhis2.mobile.commons.resources.http_error_too_many_requests
import org.dhis2.mobile.commons.resources.http_error_unauthorized
import org.dhis2.mobile.commons.resources.http_error_unknown
import org.dhis2.mobile.commons.resources.http_error_unprocessable_entity
import org.jetbrains.compose.resources.getString

class HttpStatusMessageProvider() {
    suspend fun httpStatusMessage(errorCode: Int): String =
        when (errorCode) {
            400 -> getString(Res.string.http_error_bad_request)
            401 -> getString(Res.string.http_error_unauthorized)
            403 -> getString(Res.string.http_error_forbidden)
            404 -> getString(Res.string.http_error_not_found)
            405 -> getString(Res.string.http_error_method_not_allowed)
            408 -> getString(Res.string.http_error_request_timeout)
            409 -> getString(Res.string.http_error_conflict)
            422 -> getString(Res.string.http_error_unprocessable_entity)
            429 -> getString(Res.string.http_error_too_many_requests)
            500 -> getString(Res.string.http_error_internal_server_error)
            502 -> getString(Res.string.http_error_bad_gateway)
            503 -> getString(Res.string.http_error_service_unavailable)
            504 -> getString(Res.string.http_error_gateway_timeout)
            else -> getString(Res.string.http_error_unknown)
        }
}
