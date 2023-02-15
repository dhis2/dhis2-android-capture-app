package org.dhis2.usescases.datasets.datasetDetail.datasetList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.matomo.Labels
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailRepository
import org.dhis2.utils.Action
import timber.log.Timber

class DataSetListViewModel(
    dataSetDetailRepository: DataSetDetailRepository,
    schedulerProvider: SchedulerProvider,
    val filterManager: FilterManager,
    val matomoAnalyticsController: MatomoAnalyticsController
) : ViewModel() {

    var disposable: CompositeDisposable = CompositeDisposable()
    private val _datasets = MutableLiveData<List<DataSetDetailModel>>()
    val datasets: LiveData<List<DataSetDetailModel>> = _datasets

    private val _canWrite = MutableLiveData<Boolean>()
    val canWrite: LiveData<Boolean> = _canWrite

    val progress = MutableLiveData<Boolean>()
    val selectedDataset = MutableLiveData<Action<DataSetDetailModel>>()
    val selectedSync = MutableLiveData<Action<DataSetDetailModel>>()

    init {
        disposable.add(
            filterManager.asFlowable()
                .startWith(filterManager)
                .flatMap { filterManager: FilterManager ->
                    dataSetDetailRepository.dataSetGroups(
                        filterManager.orgUnitUidsFilters,
                        filterManager.periodFilters,
                        filterManager.stateFilters,
                        filterManager.catOptComboFilters
                    )
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ _datasets.value = it }) { t: Throwable? -> Timber.d(t) }
        )

        disposable.add(
            dataSetDetailRepository.canWriteAny()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ _canWrite.value = it }) { t: Throwable? -> Timber.e(t) }
        )
    }

    fun openDataSet(dataset: DataSetDetailModel) {
        selectedDataset.postValue(Action(dataset))
    }

    fun syncDataSet(dataset: DataSetDetailModel) {
        matomoAnalyticsController.trackEvent(
            Categories.DATASET_LIST,
            Actions.SYNC_DATASET,
            Labels.CLICK
        )
        selectedSync.postValue(Action(dataset))
    }

    fun updateData() {
        filterManager.publishData()
    }
}
