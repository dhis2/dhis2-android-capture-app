package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment

import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.forms.dataentry.fields.FieldViewModel

interface EventCaptureFormView {
    fun showFields(
        fields: MutableList<FieldViewModel>,
        lastFocusItem: String
    )

    fun saveOpenedSection(it: String?)
}
