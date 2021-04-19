package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import org.dhis2.data.forms.dataentry.fields.FieldViewModel

interface EventCaptureFormView {

    fun showFields(fields: MutableList<FieldViewModel>)
    fun performSaveClick()
}
