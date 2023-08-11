package org.dhis2.Bindings

import org.hisp.dhis.android.core.common.ValidationStrategy

fun ValidationStrategy.canSkipErrorFix(hasErrorFields: Boolean, hasEmptyMandatoryFields: Boolean) =
    when (this) {
        ValidationStrategy.ON_COMPLETE -> true
        ValidationStrategy.ON_UPDATE_AND_INSERT -> !hasErrorFields && !hasEmptyMandatoryFields
    }
