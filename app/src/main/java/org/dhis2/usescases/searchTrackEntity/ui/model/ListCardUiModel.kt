package org.dhis2.usescases.searchTrackEntity.ui.model

import androidx.compose.runtime.Composable
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem

data class ListCardUiModel(
    val avatar: (@Composable () -> Unit)? = null,
    val title: String,
    val lastUpdated: String,
    val additionalInfo: List<AdditionalInfoItem>,
    val actionButton: @Composable (() -> Unit),
    val expandLabelText: String,
    val shrinkLabelText: String,
    val isOnline: Boolean,
    val onCardCLick: () -> Unit,
)
