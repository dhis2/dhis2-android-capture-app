package org.dhis2.usescases.datasets.datasetDetail.datasetList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.matomo.Labels
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.coroutine.CoroutineTracker
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailRepository
import org.dhis2.utils.Action
import timber.log.Timber

class DataSetListViewModel(
    private val dataSetDetailRepository: DataSetDetailRepository,
    val filterManager: FilterManager,
    val matomoAnalyticsController: MatomoAnalyticsController,
    dispatcher: DispatcherProvider,
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

            filterManager
                .asFlowable()
                .startWith(filterManager)
                .flatMap { filterManager: FilterManager ->
                    CoroutineTracker.increment()
                    dataSetDetailRepository.dataSetGroups(
                        filterManager.orgUnitUidsFilters,
                        filterManager.periodFilters,
                        filterManager.stateFilters,
                        filterManager.catOptComboFilters,
                    )
                }.asFlow()
                .catch {
                    Timber.d(it)
                    CoroutineTracker.decrement()
                }.collectLatest {
                    withContext(dispatcher.ui()) {
                        _datasets.value = it
                        CoroutineTracker.decrement()
                    }
                }
        }

        viewModelScope.launch(dispatcher.io()) {
            dataSetDetailRepository
                .canWriteAny()
                .asFlow()
                .catch { Timber.d(it) }
                .collectLatest {
                    withContext(dispatcher.ui()) {
                        _canWrite.value = it
                    }
                }
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
    ): Boolean =
        dataSetDetailRepository.dataSetIsEditable(
            datasetUid,
            periodId,
            organisationUnitUid,
            attributeOptionComboUid,
        )
}
