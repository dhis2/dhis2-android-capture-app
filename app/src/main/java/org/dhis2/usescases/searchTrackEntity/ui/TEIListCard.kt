package org.dhis2.usescases.searchTrackEntity.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.usescases.searchTrackEntity.ui.model.ListCardUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard

@Composable
fun ProvideTEIListCard(
    card: ListCardUiModel,
    onDownloadTei: () -> Unit,
    onTeiClick: () -> Unit,
) {
    ListCard(
        listAvatar = card.avatar,
        title = card.title,
        lastUpdated = card.lastUpdated,
        additionalInfoList = card.additionalInfo,
        actionButton = card.actionButton,
        expandLabelText = card.expandLabelText,
        shrinkLabelText = card.shrinkLabelText,
        onCardClick = {
            if (card.isOnline) {
                onDownloadTei()
            } else {
                onTeiClick()
            }
        },
    )
}

@Preview
@Composable
fun TEIListCardPreview() {
    val searchTeiModel = SearchTeiModel().apply {
        setTEType("TYPERNAMES")
    }

    ListCard(
        title = searchTeiModel.teTypeName,
        additionalInfoList = emptyList(),
        onCardClick = { },
    )
}
