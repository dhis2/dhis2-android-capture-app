package org.dhis2.usescases.teiDashboard.ui.mapper

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.DashboardProgramModel
import org.dhis2.usescases.teiDashboard.ui.model.InfoBarType
import org.dhis2.usescases.teiDashboard.ui.model.InfoBarUiModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

class InfoBarMapper(
    val resourceManager: ResourceManager,
) {
    fun map(
        infoBarType: InfoBarType,
        item: DashboardProgramModel,
        actionCallback: () -> Unit,
        showInfoBar: Boolean = false,
    ): InfoBarUiModel {
        return InfoBarUiModel(
            type = infoBarType,
            currentEnrollment = item.currentEnrollment,
            text = getText(infoBarType, item),
            textColor = getTextColor(infoBarType, item),
            icon = { GetIcon(infoBarType, item) },
            actionText = getActionText(infoBarType, item),
            onActionClick = actionCallback,
            backgroundColor = getBackgroundColor(infoBarType, item),
            showInfoBar = showInfoBar,
        )
    }

    private fun getText(
        infoBarType: InfoBarType,
        item: DashboardProgramModel,
    ): String {
        return when (infoBarType) {
            InfoBarType.SYNC -> {
                if (item.enrollmentState == State.TO_UPDATE) {
                    resourceManager.getString(R.string.not_synced)
                } else if (item.enrollmentState == State.WARNING) {
                    resourceManager.getString(R.string.sync_warning)
                } else {
                    resourceManager.getString(R.string.sync_error)
                }
            }
            InfoBarType.FOLLOW_UP -> resourceManager.getString(R.string.marked_follow_up)
            InfoBarType.ENROLLMENT_STATUS -> {
                if (item.currentEnrollmentStatus == EnrollmentStatus.COMPLETED) {
                    resourceManager.getString(R.string.enrollment_completed)
                } else {
                    resourceManager.getString(R.string.enrollment_cancelled)
                }
            }
        }
    }

    @Composable
    private fun GetIcon(
        infoBarType: InfoBarType,
        item: DashboardProgramModel,
    ) {
        return when (infoBarType) {
            InfoBarType.SYNC -> {
                if (item.enrollmentState == State.TO_UPDATE) {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = "not synced",
                        tint = TextColor.OnSurfaceLight,
                    )
                } else if (item.enrollmentState == State.WARNING) {
                    Icon(
                        imageVector = Icons.Outlined.SyncProblem,
                        contentDescription = "sync warning",
                        tint = AdditionalInfoItemColor.WARNING.color,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.SyncProblem,
                        contentDescription = "sync error",
                        tint = AdditionalInfoItemColor.ERROR.color,
                    )
                }
            }
            InfoBarType.FOLLOW_UP -> {
                Icon(
                    imageVector = Icons.Filled.Flag,
                    contentDescription = "sync warning",
                    tint = Color(0xFFFAAD14),
                )
            }
            InfoBarType.ENROLLMENT_STATUS -> {
                if (item.currentEnrollmentStatus == EnrollmentStatus.COMPLETED) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "enrollment complete",
                        tint = AdditionalInfoItemColor.SUCCESS.color,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Block,
                        contentDescription = "enrollment cancelled",
                        tint = TextColor.OnSurfaceLight,
                    )
                }
            }
        }
    }

    private fun getActionText(
        infoBarType: InfoBarType,
        item: DashboardProgramModel,
    ): String? {
        return when (infoBarType) {
            InfoBarType.SYNC -> {
                if (item.enrollmentState == State.TO_UPDATE) {
                    resourceManager.getString(R.string.sync)
                } else {
                    resourceManager.getString(R.string.sync_retry)
                }
            }
            InfoBarType.FOLLOW_UP -> resourceManager.getString(R.string.remove)
            InfoBarType.ENROLLMENT_STATUS -> null
        }
    }

    private fun getTextColor(
        infoBarType: InfoBarType,
        item: DashboardProgramModel,
    ): Color {
        return when (infoBarType) {
            InfoBarType.SYNC -> {
                if (item.enrollmentState == State.TO_UPDATE) {
                    TextColor.OnSurfaceLight
                } else if (item.enrollmentState == State.WARNING) {
                    AdditionalInfoItemColor.WARNING.color
                } else {
                    AdditionalInfoItemColor.ERROR.color
                }
            }
            InfoBarType.FOLLOW_UP -> TextColor.OnSurfaceLight
            InfoBarType.ENROLLMENT_STATUS -> {
                if (item.currentEnrollmentStatus == EnrollmentStatus.COMPLETED) {
                    AdditionalInfoItemColor.SUCCESS.color
                } else {
                    TextColor.OnSurfaceLight
                }
            }
        }
    }

    private fun getBackgroundColor(
        infoBarType: InfoBarType,
        item: DashboardProgramModel,
    ): Color {
        return when (infoBarType) {
            InfoBarType.SYNC -> {
                if (item.enrollmentState == State.TO_UPDATE) {
                    Color(0xFFEFF6FA)
                } else if (item.enrollmentState == State.WARNING) {
                    AdditionalInfoItemColor.WARNING.color.copy(alpha = 0.1f)
                } else {
                    AdditionalInfoItemColor.ERROR.color.copy(alpha = 0.1f)
                }
            }
            InfoBarType.FOLLOW_UP -> Color(0xFFEFF6FA)
            InfoBarType.ENROLLMENT_STATUS -> {
                if (item.currentEnrollmentStatus == EnrollmentStatus.COMPLETED) {
                    AdditionalInfoItemColor.SUCCESS.color.copy(alpha = 0.1f)
                } else {
                    Color(0xFFEFF6FA)
                }
            }
        }
    }
}
