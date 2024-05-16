package org.dhis2.utils.customviews

class FormBottomDialogPresenter {
    fun appendMandatoryFieldList(
        showMandatoryFields: Boolean,
        emptyMandatoryFields: Map<String, String>,
        currentMessage: String,
    ): String {
        return if (showMandatoryFields) {
            currentMessage + "\n" + emptyMandatoryFields.keys.joinToString(separator = "\n")
        } else {
            currentMessage
        }
    }
}
