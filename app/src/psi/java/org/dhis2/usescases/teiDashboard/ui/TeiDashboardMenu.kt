package org.dhis2.usescases.teiDashboard.ui

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.DashboardViewModel
import org.dhis2.usescases.teiDashboard.EnrollmentMenuItem
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData

fun customClick(
  itemId: Int,
  teiDashboardMobileActivity: TeiDashboardMobileActivity,
  programUid: String,
  enrollmentUid: String,
  teiUid: String
) {}

fun getEnrollmentMenuList(
  enrollmentUid: String?,
  resourceManager: ResourceManager,
  presenter: TeiDashboardContracts.Presenter,
  dashboardViewModel: DashboardViewModel,
): List<MenuItemData<EnrollmentMenuItem>> {
  return arrayListOf()
}
