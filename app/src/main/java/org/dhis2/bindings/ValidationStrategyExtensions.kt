package org.dhis2.bindings

import org.dhis2.form.model.EventMode
import org.hisp.dhis.android.core.common.ValidationStrategy

fun ValidationStrategy.canSkipErrorFix(
    hasErrorFields: Boolean,
    hasEmptyMandatoryFields: Boolean,
    hasEmptyEventCreationMandatoryFields: Boolean,
    eventMode: EventMode?,
) =
    when (this) {
        ValidationStrategy.ON_COMPLETE -> when (eventMode) {
            EventMode.NEW -> !hasEmptyEventCreationMandatoryFields
            else -> true
        }
        ValidationStrategy.ON_UPDATE_AND_INSERT -> !hasErrorFields && !hasEmptyMandatoryFields
    }
