package org.dhis2.commons.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.dhis2.commons.ui.model.ListCardUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState

@Composable
fun ListCardProvider(
    modifier: Modifier = Modifier,
    card: ListCardUiModel,
    title: ListCardTitleModel = ListCardTitleModel(text = card.title),
    @StringRes syncingResourceId: Int,
) {
    ListCard(
        modifier = modifier,
        listCardState =
            rememberListCardState(
                title = title,
                description = ListCardDescriptionModel(text = card.description),
                lastUpdated = card.lastUpdated,
                additionalInfoColumnState =
                    rememberAdditionalInfoColumnState(
                        additionalInfoList = card.additionalInfo,
                        syncProgressItem =
                            AdditionalInfoItem(
                                key = stringResource(id = syncingResourceId),
                                value = "",
                            ),
                        expandLabelText = card.expandLabelText,
                        shrinkLabelText = card.shrinkLabelText,
                    ),
                loading = false,
                expandable = false,
            ),
        listAvatar = card.avatar,
        onCardClick = card.onCardCLick,
        actionButton = card.actionButton,
    )
}
