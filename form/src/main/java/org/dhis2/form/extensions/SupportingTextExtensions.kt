package org.dhis2.form.extensions

import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState

fun FieldUiModel.supportingText() = listOfNotNull(
    error?.let {
        SupportingTextData(
            it,
            SupportingTextState.ERROR,
        )
    },
    warning?.let {
        SupportingTextData(
            it,
            SupportingTextState.WARNING,
        )
    },
    description?.let {
        SupportingTextData(
            it,
            SupportingTextState.DEFAULT,
        )
    },
).ifEmpty { null }
