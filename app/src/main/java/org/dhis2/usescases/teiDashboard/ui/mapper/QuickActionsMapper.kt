package org.dhis2.usescases.teiDashboard.ui.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.MoveDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.DashboardEnrollmentModel
import org.dhis2.usescases.teiDashboard.ui.model.QuickActionType
import org.dhis2.usescases.teiDashboard.ui.model.QuickActionUiModel
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

class QuickActionsMapper(
    val programUid: String?,
    val resourceManager: ResourceManager,
) {
    fun map(
        dashboardEnrollmentModel: DashboardEnrollmentModel,
        teiCanBeTransferred: Boolean,
        onActionClick: (QuickActionType) -> Unit,
    ): List<QuickActionUiModel> =
        dashboardEnrollmentModel.quickActions
            .mapNotNull {
                QuickActionType
                    .valueOf(it)
                    .filter(dashboardEnrollmentModel, teiCanBeTransferred)
            }.map { quickActionType ->
                QuickActionUiModel(
                    label = getText(quickActionType, programUid),
                    icon = getIcon(quickActionType),
                    onActionClick = { onActionClick(quickActionType) },
                )
            }

    private fun QuickActionType.filter(
        dashboardEnrollmentModel: DashboardEnrollmentModel,
        teiCanBeTransferred: Boolean,
    ): QuickActionType? =
        when (this) {
            QuickActionType.MARK_FOLLOW_UP ->
                if (dashboardEnrollmentModel.currentEnrollment.followUp() == true) {
                    null
                } else {
                    this
                }

            QuickActionType.COMPLETE_ENROLLMENT ->
                if (dashboardEnrollmentModel.currentEnrollment.status() == EnrollmentStatus.COMPLETED) {
                    QuickActionType.REOPEN_ENROLLMENT
                } else {
                    this
                }

            QuickActionType.CANCEL_ENROLLMENT ->
                if (dashboardEnrollmentModel.currentEnrollment.status() == EnrollmentStatus.CANCELLED) {
                    QuickActionType.REOPEN_ENROLLMENT
                } else {
                    this
                }

            QuickActionType.TRANSFER ->
                if (teiCanBeTransferred) {
                    this
                } else {
                    null
                }

            else -> this
        }

    private fun getText(
        quickActionType: QuickActionType,
        programUid: String?,
    ): String =
        when (quickActionType) {
            QuickActionType.MARK_FOLLOW_UP -> resourceManager.getString(R.string.mark_follow_up)
            QuickActionType.TRANSFER -> resourceManager.getString(R.string.transfer)
            QuickActionType.COMPLETE_ENROLLMENT ->
                resourceManager.formatWithEnrollmentLabel(
                    programUid,
                    R.string.complete_enrollment_label,
                    1,
                )

            QuickActionType.CANCEL_ENROLLMENT ->
                resourceManager.formatWithEnrollmentLabel(
                    programUid,
                    R.string.deactivate_enrollment_label,
                    1,
                )

            QuickActionType.REOPEN_ENROLLMENT ->
                resourceManager.formatWithEnrollmentLabel(
                    programUid,
                    R.string.reopen_enrollment_label,
                    1,
                )

            QuickActionType.MORE_ENROLLMENTS -> resourceManager.getString(R.string.more_enrollments)
        }

    private fun getIcon(quickActionType: QuickActionType) =
        @Composable {
            val iconResource =
                when (quickActionType) {
                    QuickActionType.MARK_FOLLOW_UP -> Icons.Outlined.Flag
                    QuickActionType.TRANSFER -> Icons.Outlined.MoveDown
                    QuickActionType.COMPLETE_ENROLLMENT -> Icons.Outlined.CheckCircle
                    QuickActionType.CANCEL_ENROLLMENT -> Icons.Outlined.Block
                    QuickActionType.REOPEN_ENROLLMENT -> Icons.Outlined.LockReset
                    QuickActionType.MORE_ENROLLMENTS -> Icons.AutoMirrored.Outlined.Assignment
                }
            Icon(
                imageVector = iconResource,
                contentDescription = null,
                tint =
                    if (quickActionType == QuickActionType.REOPEN_ENROLLMENT) {
                        SurfaceColor.Warning
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
        }
}
