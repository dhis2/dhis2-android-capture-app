package org.dhis2.tracker.relationships.ui.state

import org.hisp.dhis.mobile.ui.designsystem.component.SelectionState

data class ListSelectionState(
    val selectingMode: Boolean = false,
    val selectedItems: List<String> = emptyList(),
) {
    fun isSelected(
        itemUid: String,
        enabled: Boolean,
    ): SelectionState =
        when {
            !selectingMode || !enabled -> SelectionState.NONE
            selectedItems.contains(itemUid) -> SelectionState.SELECTED
            else -> SelectionState.SELECTABLE
        }
}
