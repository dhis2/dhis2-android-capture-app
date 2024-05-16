package org.dhis2.form.ui.provider.inputfield

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.hisp.dhis.android.core.common.ValueType

fun generateFieldUiModel(uid: String, value: String, displayName: String = "Test value", valueType: ValueType): FieldUiModel {

    return FieldUiModelImpl(
        uid = uid,
        layoutId = 1,
        value = value,
        focused = false,
        error = null,
        editable = true,
        warning = null,
        mandatory = false,
        label = "label",
        description = "description",
        valueType = valueType,
        allowFutureDates = true,
        displayName = displayName,
        optionSetConfiguration = null,
        autocompleteList = null,
    )
}