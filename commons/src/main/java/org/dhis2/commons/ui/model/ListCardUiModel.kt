package org.dhis2.commons.ui.model

import androidx.compose.runtime.Composable
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem

data class ListCardUiModel(
    val avatar: (@Composable () -> Unit)? = null,
    val title: String,
    val description: String? = null,
    val lastUpdated: String? = null,
    val additionalInfo: List<AdditionalInfoItem>,
    val actionButton: @Composable (() -> Unit),
    val expandLabelText: String,
    val shrinkLabelText: String,
    val onCardCLick: () -> Unit,
)
