package org.dhis2.mobile.commons.resources

interface D2ErrorMessageProvider {
    suspend fun getErrorMessage(throwable: Throwable): String?
}
