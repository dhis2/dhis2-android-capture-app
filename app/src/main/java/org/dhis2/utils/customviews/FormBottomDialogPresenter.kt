package org.dhis2.utils.customviews

import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.image.ImageViewModel

class FormBottomDialogPresenter {
    fun appendMandatoryFieldList(
        showMandatoryFields: Boolean,
        emptyMandatoryFields: Map<String, FieldViewModel>,
        currentMessage: String
    ): String {
        return if (showMandatoryFields) {
            currentMessage + "\n" + emptyMandatoryFields.values.joinToString(separator = "\n") {
                if (it is ImageViewModel) {
                    it.fieldDisplayName()
                } else {
                    it.label()
                }
            }
        } else {
            currentMessage
        }
    }
}
