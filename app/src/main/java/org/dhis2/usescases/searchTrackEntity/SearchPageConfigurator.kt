package org.dhis2.usescases.searchTrackEntity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class SearchPageConfigurator(
    val searchRepository: SearchRepository,
    val schedulerProvider: SchedulerProvider
) : NavigationPageConfigurator {

    override fun displayListView(): Boolean {
        return true
    }

    override fun displayTableView(): Boolean {
        return false
    }

    override fun displayMapView(): Boolean {
        return runBlocking {
            return@runBlocking withContext(Dispatchers.IO) {
                searchRepository.programHasCoordinates()
            }
        }
    }

    override fun displayAnalytics(): Boolean {
        return searchRepository.programHasAnalytics()
    }
}
