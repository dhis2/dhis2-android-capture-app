package org.dhis2.ui.dialogs.bottomsheet

data class BottomSheetDialogUiModel(
    var title: String,
    var subtitle: String? = null,
    var message: String? = null,
    var clickableWord: String? = null,
    var iconResource: Int,
    var mainButton: DialogButtonStyle?=null,
    var secondaryButton: DialogButtonStyle? = null
)
