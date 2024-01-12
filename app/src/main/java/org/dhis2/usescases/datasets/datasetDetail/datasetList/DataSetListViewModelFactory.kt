package org.dhis2.usescases.datasets.datasetDetail.datasetList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailRepository

@Suppress("UNCHECKED_CAST")
class DataSetListViewModelFactory(
    val dataSetDetailRepository: DataSetDetailRepository,
    val schedulerProvider: SchedulerProvider,
    val filterManager: FilterManager,
    val matomoAnalyticsController: MatomoAnalyticsController,
    private val dispatchers: DispatcherProvider,

) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DataSetListViewModel(
            dataSetDetailRepository,
            schedulerProvider,
            filterManager,
            matomoAnalyticsController,
            dispatchers,
        ) as T
    }
}
