package org.dhis2.usescases.teiDashboard.ui.model

import androidx.compose.runtime.Composable

data class QuickActionUiModel(
    val label: String,
    val icon: @Composable () -> Unit,
    val onActionClick: () -> Unit,
)
