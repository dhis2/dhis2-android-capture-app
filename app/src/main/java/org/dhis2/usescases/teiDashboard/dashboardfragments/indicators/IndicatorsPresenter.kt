package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import timber.log.Timber

class IndicatorsPresenter(
    val schedulerProvider: SchedulerProvider,
    val view: IndicatorsView,
    val indicatorRepository: IndicatorRepository
) {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        compositeDisposable.add(
            indicatorRepository.fetchData()
                .defaultSubscribe(schedulerProvider, { view.swapAnalytics(it) }, { Timber.d(it) })
        )
    }

    fun onDettach() = compositeDisposable.clear()

    fun displayMessage(message: String) = view.displayMessage(message)
}
