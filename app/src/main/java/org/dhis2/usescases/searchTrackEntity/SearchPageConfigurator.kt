package org.dhis2.usescases.searchTrackEntity

import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class SearchPageConfigurator(
    val searchRepository: SearchRepository
) : NavigationPageConfigurator {

    override fun displayListView(): Boolean {
        return true
    }

    override fun displayTableView(): Boolean {
        return false
    }

    override fun displayMapView(): Boolean {
        return searchRepository.programHasCoordinates()
    }

    override fun displayAnalytics(): Boolean {
        return searchRepository.programHasAnalytics()
    }
}
