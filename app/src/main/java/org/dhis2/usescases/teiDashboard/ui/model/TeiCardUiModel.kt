package org.dhis2.usescases.teiDashboard.ui.model

import androidx.compose.runtime.Composable
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem

data class TeiCardUiModel(
    val avatar: (@Composable () -> Unit)? = null,
    val title: String,
    val additionalInfo: List<AdditionalInfoItem>,
    val actionButton: @Composable (() -> Unit),
    val expandLabelText: String,
    val shrinkLabelText: String,
    val showLoading: Boolean,
)
