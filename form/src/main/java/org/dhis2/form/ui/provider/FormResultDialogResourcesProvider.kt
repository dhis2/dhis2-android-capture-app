package org.dhis2.form.ui.provider

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R

class FormResultDialogResourcesProvider(
    private val resourceManager: ResourceManager,
) {
    fun provideRedAlertIcon() = R.drawable.ic_error_outline

    fun provideYellowAlertIcon() = R.drawable.ic_warning_alert

    fun provideSavedIcon() = R.drawable.ic_saved_check

    fun provideNotSavedText() = resourceManager.getString(R.string.not_saved)

    fun provideSavedText() = resourceManager.getString(R.string.saved)

    fun provideDiscardWarning() = resourceManager.getString(R.string.discard_go_back)

    fun provideErrorInfo() = resourceManager.getString(R.string.missing_error_fields_events)

    fun provideErrorWithDiscard() = resourceManager.getString(R.string.field_errors_not_saved_discard)

    fun provideMandatoryInfo() = resourceManager.getString(R.string.review_message)

    fun provideMandatoryField() = resourceManager.getString(R.string.field_is_mandatory)

    fun provideWarningInfo() = resourceManager.getString(R.string.missing_warning_fields_events)

    fun provideWarningInfoCompletedEvent() = resourceManager.getString(R.string.missing_warning_fields_completed_events)

    fun provideCompleteInfo() = resourceManager.getString(R.string.event_can_be_completed)

    fun provideOnCompleteErrorInfo() = resourceManager.getString(R.string.event_error_on_complete)
}
