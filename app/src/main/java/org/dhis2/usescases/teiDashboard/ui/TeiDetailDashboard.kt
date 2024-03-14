package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        if (syncData.showInfoBar) {
            InfoBar(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .testTag(SYNC_INFO_BAR_TEST_TAG),
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
            if (followUpData.showInfoBar || enrollmentData.showInfoBar) {
                Spacer(modifier = Modifier.padding(top = 8.dp))
            }
        }

        if (followUpData.showInfoBar) {
            InfoBar(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .testTag(FOLLOWUP_INFO_BAR_TEST_TAG),
                infoBarData = InfoBarData(
                    text = followUpData.text,
                    icon = followUpData.icon,
                    color = followUpData.textColor,
                    backgroundColor = followUpData.backgroundColor,
                    actionText = followUpData.actionText,
                    onClick = followUpData.onActionClick,
                ),
            )
            if (enrollmentData.showInfoBar) {
                Spacer(modifier = Modifier.padding(top = 8.dp))
            }
        }

        if (enrollmentData.showInfoBar) {
            InfoBar(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .testTag(STATE_INFO_BAR_TEST_TAG),
                infoBarData = InfoBarData(
                    text = enrollmentData.text,
                    icon = enrollmentData.icon,
                    color = enrollmentData.textColor,
                    backgroundColor = enrollmentData.backgroundColor,
                    actionText = enrollmentData.actionText,
                ),
            )
        }

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

const val SYNC_INFO_BAR_TEST_TAG = "sync"
const val FOLLOWUP_INFO_BAR_TEST_TAG = "followUp"
const val STATE_INFO_BAR_TEST_TAG = "state"
