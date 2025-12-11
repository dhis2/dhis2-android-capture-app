package org.dhis2.form.ui.provider

import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType

inline fun onFieldFocusChanged(
    fieldUid: String,
    value: String,
    valueType: ValueType?,
    isFocused: Boolean,
    intentHandler: (FormIntent) -> Unit,
) {
    if (!isFocused) {
        intentHandler(
            FormIntent.OnSave(
                fieldUid,
                value,
                valueType,
            ),
        )
    }
}
