package org.dhis2.composetable.ui.extensions

import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi

@OptIn(ExperimentalMaterialApi::class)
suspend fun BottomSheetState.collapseIfExpanded(onCollapse: () -> Unit) {
    if (isExpanded) {
        onCollapse()
        collapse()
    }
}

@OptIn(ExperimentalMaterialApi::class)
suspend fun BottomSheetState.expandIfCollapsed(onExpand: () -> Unit) {
    if (isCollapsed) {
        onExpand()
        expand()
    }
}
