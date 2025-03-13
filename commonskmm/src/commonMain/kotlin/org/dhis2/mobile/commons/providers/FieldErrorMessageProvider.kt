package org.dhis2.mobile.commons.providers

expect class FieldErrorMessageProvider {
    suspend fun getFriendlyErrorMessage(error: Throwable): String
}
