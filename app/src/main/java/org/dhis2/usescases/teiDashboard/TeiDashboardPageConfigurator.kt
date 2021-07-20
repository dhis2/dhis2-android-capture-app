package org.dhis2.usescases.teiDashboard

import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class TeiDashboardPageConfigurator(
    val dashboardRepository: DashboardRepository
) : NavigationPageConfigurator {

    override fun displayDetails(): Boolean {
        return true
    }

    override fun displayAnalytics(): Boolean {
        return dashboardRepository.programHasAnalytics()
    }

    override fun displayRelationships(): Boolean {
        return dashboardRepository.programHasRelationships()
    }

    override fun displayNotes(): Boolean {
        return true
    }
}
