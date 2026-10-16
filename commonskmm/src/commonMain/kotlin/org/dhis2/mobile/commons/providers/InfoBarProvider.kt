package org.dhis2.mobile.commons.providers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.enrollment_cancelled
import org.dhis2.mobile.commons.resources.enrollment_completed
import org.dhis2.mobile.commons.resources.marked_follow_up
import org.dhis2.mobile.commons.resources.not_synced
import org.dhis2.mobile.commons.resources.remove_follow_up
import org.dhis2.mobile.commons.resources.sync
import org.dhis2.mobile.commons.resources.sync_error
import org.dhis2.mobile.commons.resources.sync_retry
import org.dhis2.mobile.commons.resources.sync_warning
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.jetbrains.compose.resources.stringResource

@Composable
fun InfoBarProvider(
    dataModel: InfoBarUiModel,
    onActionClick: (() -> Unit)?,
    modifier: Modifier,
) {
    when (dataModel.type) {
        InfoBarType.PENDING_SYNC -> {
            InfoBar(
                modifier =
                    modifier
                        .testTag(INFO_BAR_TEST_TAG + InfoBarType.PENDING_SYNC.name),
                text = stringResource(Res.string.not_synced),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = "not synced",
                        tint = TextColor.OnSurfaceLight,
                    )
                },
                textColor = TextColor.OnSurfaceLight,
                backgroundColor = Color(0xFFEFF6FA),
                actionText = stringResource(Res.string.sync),
                onActionClick = {
                    onActionClick?.invoke()
                },
            )
        }
        InfoBarType.SYNC_WARNING -> {
            InfoBar(
                modifier =
                    modifier
                        .testTag(INFO_BAR_TEST_TAG + InfoBarType.SYNC_WARNING.name),
                text = stringResource(Res.string.sync_warning),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.SyncProblem,
                        contentDescription = "sync warning",
                        tint = AdditionalInfoItemColor.WARNING.color,
                    )
                },
                textColor = AdditionalInfoItemColor.WARNING.color,
                backgroundColor = AdditionalInfoItemColor.WARNING.color.copy(alpha = 0.1f),
                actionText = stringResource(Res.string.sync_retry),
                onActionClick = {
                    onActionClick?.invoke()
                },
            )
        }
        InfoBarType.SYNC_ERROR -> {
            InfoBar(
                modifier =
                    modifier
                        .testTag(INFO_BAR_TEST_TAG + InfoBarType.SYNC_ERROR.name),
                text = stringResource(Res.string.sync_error),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.SyncProblem,
                        contentDescription = "sync error",
                        tint = AdditionalInfoItemColor.ERROR.color,
                    )
                },
                textColor = AdditionalInfoItemColor.ERROR.color,
                backgroundColor = AdditionalInfoItemColor.ERROR.color.copy(alpha = 0.1f),
                actionText = stringResource(Res.string.sync_retry),
                onActionClick = {
                    onActionClick?.invoke()
                },
            )
        }
        InfoBarType.FOLLOW_UP -> {
            InfoBar(
                modifier =
                    modifier
                        .testTag(INFO_BAR_TEST_TAG + InfoBarType.FOLLOW_UP.name),
                text = stringResource(Res.string.marked_follow_up),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = "follow-up",
                        tint = Color(0xFFFAAD14),
                    )
                },
                textColor = TextColor.OnSurfaceLight,
                backgroundColor = Color(0xFFEFF6FA),
                actionText = stringResource(Res.string.remove_follow_up),
                onActionClick = {
                    onActionClick?.invoke()
                },
            )
        }
        InfoBarType.ENROLLMENT_COMPLETED -> {
            InfoBar(
                modifier =
                    modifier
                        .testTag(INFO_BAR_TEST_TAG + InfoBarType.ENROLLMENT_COMPLETED.name),
                text = dataModel.text ?: stringResource(Res.string.enrollment_completed),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "enrollment complete",
                        tint = AdditionalInfoItemColor.SUCCESS.color,
                    )
                },
                textColor = AdditionalInfoItemColor.SUCCESS.color,
                backgroundColor = AdditionalInfoItemColor.SUCCESS.color.copy(alpha = 0.1f),
                actionText = "",
                onActionClick = {
                },
            )
        }
        InfoBarType.ENROLLMENT_CANCELLED -> {
            InfoBar(
                modifier =
                    modifier
                        .testTag(INFO_BAR_TEST_TAG + InfoBarType.ENROLLMENT_CANCELLED.name),
                text = dataModel.text ?: stringResource(Res.string.enrollment_cancelled),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Block,
                        contentDescription = "enrollment cancelled",
                        tint = TextColor.OnSurfaceLight,
                    )
                },
                textColor = TextColor.OnSurfaceLight,
                backgroundColor = Color(0xFFEFF6FA),
                actionText = "",
                onActionClick = {
                },
            )
        }
    }
}

data class InfoBarUiModel(
    val type: InfoBarType,
    val text: String? = null,
)

enum class InfoBarType {
    PENDING_SYNC,
    SYNC_WARNING,
    SYNC_ERROR,
    FOLLOW_UP,
    ENROLLMENT_COMPLETED,
    ENROLLMENT_CANCELLED,
}

const val INFO_BAR_TEST_TAG = "INFO_BAR_TEST_TAG"
