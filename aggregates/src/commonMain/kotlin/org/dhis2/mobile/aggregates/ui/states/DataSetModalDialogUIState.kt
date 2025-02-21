package org.dhis2.mobile.aggregates.ui.states

import androidx.compose.runtime.Composable
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState

internal data class DataSetModalDialogUIState(
    val contentDialogUIState: BottomSheetShellUIState,
    val content: @Composable (() -> Unit)? = null,
    val buttonsDialog: @Composable (() -> Unit),
    val onDismiss: () -> Unit,
)
