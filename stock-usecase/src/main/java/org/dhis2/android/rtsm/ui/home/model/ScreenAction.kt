package org.dhis2.android.rtsm.ui.home.model

sealed interface ScreenAction {
    data class OpenAnalytics(
        val containerId: Int,
    ) : ScreenAction

    data object OpenOrgUnitTree : ScreenAction

    data object OpenManageStockBottomSheet : ScreenAction

    data class OnDiscardTransaction(
        val onResult: (result: EditionDialogResult) -> Unit,
    ) : ScreenAction
}
