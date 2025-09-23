package org.dhis2.commons.ui

import androidx.annotation.StringRes
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.dhis2.commons.ui.model.ListCardUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun ListCardProvider(
    modifier: Modifier = Modifier,
    card: ListCardUiModel,
    @StringRes syncingResourceId: Int,
) {
    ListCard(
        modifier = modifier,
        listCardState =
            rememberListCardState(
                title =
                    ListCardTitleModel(
                        text = card.title,
                        style =
                            LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight(500),
                                lineHeight = 20.sp,
                            ),
                        color = TextColor.OnSurface,
                    ),
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
