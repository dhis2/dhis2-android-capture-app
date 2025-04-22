package org.dhis2.usescases.teiDashboard

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
    return emptyArray()
}
