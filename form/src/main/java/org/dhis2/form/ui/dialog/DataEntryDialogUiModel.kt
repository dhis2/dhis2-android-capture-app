package org.dhis2.form.ui.dialog

import org.dhis2.commons.data.FieldWithIssue

data class DataEntryDialogUiModel(
    var title: String,
    var subtitle: String,
    var iconResource: Int,
    var fieldsWithIssues: List<FieldWithIssue> = emptyList(),
    var mainButton: DialogButtonStyle,
    var secondaryButton: DialogButtonStyle? = null
)
