package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.commons.data.EventCreationType
import org.dhis2.usescases.teiDashboard.ui.model.InfoBarUiModel
import org.dhis2.usescases.teiDashboard.ui.model.QuickActionUiModel
import org.dhis2.usescases.teiDashboard.ui.model.TeiCardUiModel
import org.dhis2.usescases.teiDashboard.ui.model.TimelineEventsHeaderModel
import org.hisp.dhis.mobile.ui.designsystem.component.AssistChip
import org.hisp.dhis.mobile.ui.designsystem.component.CardDetail
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBarData
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

@Composable
fun TeiDetailDashboard(
    infoBarModels: List<InfoBarUiModel>,
    card: TeiCardUiModel?,
    timelineEventHeaderModel: TimelineEventsHeaderModel,
    timelineOnEventCreationOptionSelected: (EventCreationType) -> Unit,
    quickActions: List<QuickActionUiModel>,
    modifier: Modifier = Modifier,
    isGrouped: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            infoBarModels.forEach { infoBar ->
                if (infoBar.showInfoBar) {
                    InfoBar(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .testTag(INFO_BAR_TEST_TAG + infoBar.type.name),
                        infoBarData = InfoBarData(
                            text = infoBar.text,
                            icon = infoBar.icon,
                            color = infoBar.textColor,
                            backgroundColor = infoBar.backgroundColor,
                            actionText = infoBar.actionText,
                            onClick = infoBar.onActionClick,
                        ),
                    )
                }
            }
        }

        card?.let {
            CardDetail(
                title = card.title,
                additionalInfoList = card.additionalInfo,
                avatar = card.avatar,
                actionButton = card.actionButton,
                expandLabelText = card.expandLabelText,
                shrinkLabelText = card.shrinkLabelText,
                showLoading = card.showLoading,
            )
        }

        QuickActionsRow(quickActions)

        if (!isGrouped) {
            TimelineEventsHeader(
                timelineEventsHeaderModel = timelineEventHeaderModel,
                onOptionSelected = timelineOnEventCreationOptionSelected,
            )
        }
    }
}

@Composable
private fun QuickActionsRow(quickActions: List<QuickActionUiModel>) {
    if (quickActions.isNotEmpty()) {
        LazyRow(
            modifier = Modifier.padding(bottom = Spacing.Spacing16),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
            contentPadding = PaddingValues(horizontal = Spacing.Spacing16),
        ) {
            items(quickActions) {
                AssistChip(
                    label = it.label,
                    icon = it.icon,
                    onClick = it.onActionClick,
                )
            }
        }
    }
}

const val INFO_BAR_TEST_TAG = "INFO_BAR_TEST_TAG"
