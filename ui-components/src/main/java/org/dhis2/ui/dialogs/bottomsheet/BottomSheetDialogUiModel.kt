package org.dhis2.ui.dialogs.bottomsheet

data class BottomSheetDialogUiModel(
    var title: String,
    var subtitle: String,
    var clickableWord:String? = null,
    var iconResource: Int,
    var fieldsWithIssues: List<FieldWithIssue> = emptyList(),
    var mainButton: DialogButtonStyle,
    var secondaryButton: DialogButtonStyle? = null
)
