package org.dhis2.commons.dialogs.bottomsheet

import androidx.compose.ui.text.style.TextAlign

data class BottomSheetDialogUiModel(
    var title: String,
    var subtitle: String? = null,
    var message: String? = null,
    var clickableWord: String? = null,
    var iconResource: Int,
    var headerTextAlignment: TextAlign = TextAlign.Center,
    var mainButton: DialogButtonStyle? = null,
    var secondaryButton: DialogButtonStyle? = null,
) {
    fun hasButtons() = mainButton != null || secondaryButton != null

    fun secondaryRoundedCornersSizeDp() =
        if (secondaryButton is DialogButtonStyle.NeutralButton) {
            2
        } else {
            0
        }

    fun secondaryElevationDp() =
        if (secondaryButton is DialogButtonStyle.NeutralButton) {
            24
        } else {
            0
        }
}
