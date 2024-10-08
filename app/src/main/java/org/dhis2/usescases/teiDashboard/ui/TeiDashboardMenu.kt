package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.MoveDown
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Workspaces
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.DashboardEnrollmentModel
import org.dhis2.usescases.teiDashboard.DashboardViewModel
import org.dhis2.usescases.teiDashboard.EnrollmentMenuItem
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemStyle
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuLeadingElement
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

fun getEnrollmentMenuList(
    enrollmentUid: String?,
    resourceManager: ResourceManager,
    presenter: TeiDashboardContracts.Presenter,
    dashboardViewModel: DashboardViewModel,
): List<MenuItemData<EnrollmentMenuItem>> {
    return if (enrollmentUid == null) {
        buildList {
            addAll(
                listOf(
                    getSyncMenuItem(resourceManager),
                    getMoreEnrollmentsMenuItem(resourceManager),
                ),
            )
            getDeleteTEIMenuItem(presenter, resourceManager)?.let { add(it) }
        }
    } else {
        buildList {
            add(getSyncMenuItem(resourceManager))
            if (dashboardViewModel.checkIfTeiCanBeTransferred()) {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.TRANSFER,
                        label = resourceManager.getString(R.string.transfer),
                        leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.MoveDown),
                    ),
                )
            }

            if (!dashboardViewModel.showFollowUpBar.value) {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.FOLLOW_UP,
                        label = resourceManager.getString(R.string.mark_follow_up),
                        leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Flag),
                    ),
                )
            }
            if (dashboardViewModel.groupByStage.value != false) {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.VIEW_TIMELINE,
                        label = resourceManager.getString(R.string.view_timeline),
                        leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Timeline),
                    ),
                )
            }
            if (dashboardViewModel.groupByStage.value == false) {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.GROUP_BY_STAGE,
                        label = resourceManager.getString(R.string.group_by_stage),
                        leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Workspaces),
                    ),
                )
            }
            addAll(
                listOf(
                    MenuItemData(
                        id = EnrollmentMenuItem.HELP,
                        label = resourceManager.getString(R.string.showHelp),
                        leadingElement = MenuLeadingElement.Icon(icon = Icons.AutoMirrored.Outlined.HelpOutline),
                    ),
                    getMoreEnrollmentsMenuItem(resourceManager),
                    MenuItemData(
                        id = EnrollmentMenuItem.SHARE,
                        label = resourceManager.getString(R.string.share),
                        showDivider = true,
                        leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Share),
                    ),
                ),
            )

            val status = presenter.getEnrollmentStatus(enrollmentUid)
            if (status != EnrollmentStatus.COMPLETED) {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.COMPLETE,
                        label = resourceManager.getString(R.string.complete),
                        leadingElement = MenuLeadingElement.Icon(
                            icon = Icons.Outlined.CheckCircle,
                            defaultTintColor = SurfaceColor.CustomGreen,
                            selectedTintColor = SurfaceColor.CustomGreen,
                        ),
                    ),
                )
            }

            if (status != EnrollmentStatus.ACTIVE) {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.ACTIVATE,
                        label = resourceManager.getString(R.string.re_open),
                        showDivider = status == EnrollmentStatus.CANCELLED,
                        leadingElement = MenuLeadingElement.Icon(
                            icon = Icons.Outlined.LockReset,
                            defaultTintColor = SurfaceColor.Warning,
                            selectedTintColor = SurfaceColor.Warning,
                        ),
                    ),
                )
            }

            if (status != EnrollmentStatus.CANCELLED) {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.DEACTIVATE,
                        label = resourceManager.getString(R.string.deactivate),
                        showDivider = true,
                        leadingElement = MenuLeadingElement.Icon(
                            icon = Icons.Outlined.Cancel,
                            defaultTintColor = TextColor.OnDisabledSurface,
                            selectedTintColor = TextColor.OnDisabledSurface,
                        ),
                    ),
                )
            }

            if (presenter.checkIfEnrollmentCanBeDeleted(enrollmentUid)) {
                val dashboardModel = dashboardViewModel.dashboardModel.value
                val programmeName = if (dashboardModel is DashboardEnrollmentModel) {
                    dashboardModel.currentProgram().displayName()
                } else {
                    ""
                }
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.REMOVE,
                        label = resourceManager.getString(R.string.remove_from),
                        supportingText = programmeName,
                        style = MenuItemStyle.ALERT,
                        leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.DeleteOutline),
                    ),
                )
            }

            getDeleteTEIMenuItem(presenter, resourceManager)?.let { add(it) }
        }
    }
}

fun getSyncMenuItem(
    resourceManager: ResourceManager,
): MenuItemData<EnrollmentMenuItem> {
    return MenuItemData(
        id = EnrollmentMenuItem.SYNC,
        label = resourceManager.getString(R.string.refresh_this_record),
        leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Sync),
    )
}

fun getMoreEnrollmentsMenuItem(
    resourceManager: ResourceManager,
): MenuItemData<EnrollmentMenuItem> {
    return MenuItemData(
        id = EnrollmentMenuItem.ENROLLMENTS,
        label = resourceManager.getString(R.string.more_enrollments),
        leadingElement = MenuLeadingElement.Icon(icon = Icons.AutoMirrored.Outlined.Assignment),
    )
}

fun getDeleteTEIMenuItem(
    presenter: TeiDashboardContracts.Presenter,
    resourceManager: ResourceManager,
): MenuItemData<EnrollmentMenuItem>? {
    return if (presenter.checkIfTEICanBeDeleted()) {
        MenuItemData(
            id = EnrollmentMenuItem.DELETE,
            label = resourceManager.getString(R.string.dashboard_menu_delete_tei_v2, presenter.teType),
            style = MenuItemStyle.ALERT,
            leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.DeleteForever),
        )
    } else {
        null
    }
}
