package org.dhis2.usescases.searchTrackEntity

import io.reactivex.Single
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
       return Single.fromCallable { searchRepository.programHasCoordinates() }
           .subscribeOn(schedulerProvider.ui())
           .observeOn(schedulerProvider.ui())
           .blockingGet()
      /*  Observable.fromCallable(searchRepository.programHasCoordinates())
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
*/

        //Observable.fromCallable(() -> decompress(decodeData(inputData).getBytes()))
    }

    override fun displayAnalytics(): Boolean {
        return searchRepository.programHasAnalytics()
    }
}
