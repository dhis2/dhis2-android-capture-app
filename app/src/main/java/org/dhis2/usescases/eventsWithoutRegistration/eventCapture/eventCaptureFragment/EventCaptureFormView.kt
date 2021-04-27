package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import org.dhis2.form.data.FieldUiModel

interface EventCaptureFormView {

    fun showFields(fields: MutableList<FieldUiModel>)
    fun performSaveClick()
}
