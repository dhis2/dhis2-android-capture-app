package org.dhis2.usescases.datasets.datasetDetail.datasetList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.matomo.Labels
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailRepository
import org.dhis2.utils.Action
import timber.log.Timber

class DataSetListViewModel(
    private val dataSetDetailRepository: DataSetDetailRepository,
    schedulerProvider: SchedulerProvider,
    val filterManager: FilterManager,
    val matomoAnalyticsController: MatomoAnalyticsController,
    private val dispatcher: DispatcherProvider,

) : ViewModel() {

    private val _datasets = MutableLiveData<List<DataSetDetailModel>>()
    val datasets: LiveData<List<DataSetDetailModel>> = _datasets

    private val _canWrite = MutableLiveData<Boolean>()
    val canWrite: LiveData<Boolean> = _canWrite

    val progress = MutableLiveData<Boolean>()
    val selectedDataset = MutableLiveData<Action<DataSetDetailModel>>()
    val selectedSync = MutableLiveData<Action<DataSetDetailModel>>()

    init {

        viewModelScope.launch(dispatcher.io()) {

            val datasets = async {
                filterManager.asFlowable()
                    .startWith(filterManager)
                    .flatMap { filterManager: FilterManager ->

                        dataSetDetailRepository.dataSetGroups(
                            filterManager.orgUnitUidsFilters,
                            filterManager.periodFilters,
                            filterManager.stateFilters,
                            filterManager.catOptComboFilters,
                        )
                    }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe({
                        _datasets.value = it
                    }) { t: Throwable? -> Timber.d(t) }
            }
            val permissions = async {
                dataSetDetailRepository.canWriteAny()
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe({
                        _canWrite.value = it
                    }) { t: Throwable? -> Timber.e(t) }
            }
            datasets.await()
            permissions.await()
        }
    }

    fun openDataSet(dataset: DataSetDetailModel) {
        selectedDataset.postValue(Action(dataset))
    }

    fun syncDataSet(dataset: DataSetDetailModel) {
        matomoAnalyticsController.trackEvent(
            Categories.DATASET_LIST,
            Actions.SYNC_DATASET,
            Labels.CLICK,
        )
        selectedSync.postValue(Action(dataset))
    }

    fun updateData() {
        filterManager.publishData()
    }

    fun isEditable(
        datasetUid: String,
        periodId: String,
        organisationUnitUid: String,
        attributeOptionComboUid: String,
    ): Boolean {
        return dataSetDetailRepository.dataSetIsEditable(
            datasetUid,
            periodId,
            organisationUnitUid,
            attributeOptionComboUid,
        )
    }
}
