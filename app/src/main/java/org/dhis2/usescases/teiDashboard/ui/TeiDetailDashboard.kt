package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dhis2.usescases.teiDashboard.ui.model.InfoBarUiModel
import org.dhis2.usescases.teiDashboard.ui.model.TeiCardUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.CardDetail
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBarData

@Composable
fun TeiDetailDashboard(
    syncData: InfoBarUiModel,
    followUpData: InfoBarUiModel,
    enrollmentData: InfoBarUiModel,
    card: TeiCardUiModel,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = spacedBy(8.dp),
    ) {
        if (syncData.showInfoBar) {
            item {
                InfoBar(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
                    infoBarData =
                    InfoBarData(
                        text = syncData.text,
                        icon = syncData.icon,
                        color = syncData.textColor,
                        backgroundColor = syncData.backgroundColor,
                        actionText = syncData.actionText,
                        onClick = syncData.onActionClick,
                    ),
                )
            }
        }
        if (followUpData.showInfoBar) {
            item {
                InfoBar(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
                    infoBarData = InfoBarData(
                        text = followUpData.text,
                        icon = followUpData.icon,
                        color = followUpData.textColor,
                        backgroundColor = followUpData.backgroundColor,
                        actionText = followUpData.actionText,
                        onClick = followUpData.onActionClick,
                    ),
                )
            }
        }
        if (enrollmentData.showInfoBar) {
            item {
                InfoBar(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
                    infoBarData = InfoBarData(
                        text = enrollmentData.text,
                        icon = enrollmentData.icon,
                        color = enrollmentData.textColor,
                        backgroundColor = enrollmentData.backgroundColor,
                        actionText = enrollmentData.actionText,
                    ),
                )
            }
        }
        item {
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
    }
}
