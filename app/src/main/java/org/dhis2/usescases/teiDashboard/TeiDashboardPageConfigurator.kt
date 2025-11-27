package org.dhis2.usescases.teiDashboard

import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class TeiDashboardPageConfigurator(
    val dashboardRepository: DashboardRepository,
    val isPortrait: Boolean,
) : NavigationPageConfigurator {
    override fun displayDetails(): Boolean = isPortrait

    override fun displayAnalytics(): Boolean = dashboardRepository.programHasAnalytics()

    override fun displayRelationships(): Boolean = dashboardRepository.programHasRelationships()

    override fun displayNotes(): Boolean = true
}
