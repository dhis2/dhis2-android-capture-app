package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.provider

import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager

class EventCaptureResourcesProvider(
    private val resourceManager: ResourceManager

) {
    fun provideRedAlertIcon() = R.drawable.ic_error_outline

    fun provideYellowAlertIcon() = R.drawable.ic_warning_alert

    fun provideSavedIcon() = R.drawable.ic_saved_check

    fun provideNotSavedText() = resourceManager.getString(R.string.not_saved)

    fun provideSavedText() = resourceManager.getString(R.string.saved)

    fun provideErrorInfo() = resourceManager.getString(R.string.missing_error_fields_events)

    fun provideMandatoryInfo() = resourceManager.getString(R.string.missing_mandatory_fields_events)

    fun provideMandatoryField() = resourceManager.getString(R.string.field_is_mandatory)

    fun provideWarningInfo() = resourceManager.getString(R.string.missing_warning_fields_events)

    fun provideReview() = R.string.review

    fun provideNotNow() = R.string.not_now

    fun provideCompleteInfo() = resourceManager.getString(R.string.event_can_be_completed)

    fun provideOnCompleteErrorInfo() = resourceManager.getString(R.string.event_error_on_complete)
}
