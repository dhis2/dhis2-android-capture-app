package org.dhis2.mobile.aggregates.ui.states

import org.dhis2.mobile.aggregates.model.Violation
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState

internal data class DataSetModalDialogUIState(
    val contentDialogUIState: BottomSheetShellUIState,
    val onDismiss: () -> Unit,
    val onPrimaryButtonClick: () -> Unit,
    val onSecondaryButtonClick: () -> Unit = {},
    val type: DataSetModalType,
    val violations: List<Violation>? = null,
    val mandatory: Boolean = false,
    val canComplete: Boolean = true,
)

internal enum class DataSetModalType {
    COMPLETION,
    MANDATORY_FIELDS,
    VALIDATION_RULES,
    VALIDATION_RULES_ERROR,
}
