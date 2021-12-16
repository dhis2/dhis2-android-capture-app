package org.dhis2.utils.customviews

import org.dhis2.form.model.FieldUiModel

class FormBottomDialogPresenter {
    fun appendMandatoryFieldList(
        showMandatoryFields: Boolean,
        emptyMandatoryFields: Map<String, FieldUiModel>,
        currentMessage: String
    ): String {
        return if (showMandatoryFields) {
            currentMessage + "\n" + emptyMandatoryFields.values.joinToString(separator = "\n") {
                it.label
            }
        } else {
            currentMessage
        }
    }
}
