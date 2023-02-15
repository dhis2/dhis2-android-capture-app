package org.dhis2.commons.dialogs.bottomsheet

import org.dhis2.commons.data.FieldWithIssue

data class BottomSheetDialogUiModel(
    var title: String,
    var subtitle: String,
    var iconResource: Int,
    var fieldsWithIssues: List<FieldWithIssue> = emptyList(),
    var mainButton: DialogButtonStyle,
    var secondaryButton: DialogButtonStyle? = null
)
