package org.dhis2.android.rtsm.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.android.rtsm.data.models.StockItem
import org.dhis2.android.rtsm.services.MetadataManager
import org.dhis2.android.rtsm.services.preferences.PreferenceProvider
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.ui.base.BaseViewModel
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    preferenceProvider: PreferenceProvider,
    //private val filterManager: FilterManager,
    //private val filterRepository: FilterRepository,
    private val metadataManager: MetadataManager,
    savedState: SavedStateHandle
) : BaseViewModel(preferenceProvider, schedulerProvider) {

    private var _stockItems = MutableLiveData<List<StockItem>>()
    var stockItem: LiveData<List<StockItem>> = _stockItems

    init {
        _stockItems.value = mutableListOf(
            StockItem("1", "First", "No Hand"),
            StockItem("2", "Second", "No Hand"),
            StockItem("3", "Third", "No Hand"),
        )
    }

    /*fun initFilters(
        hideFilters: () -> Unit,
        setFilters: (filters: List<FilterItem>) -> Unit
    ) {
        disposable.add(
            Flowable.just(filterRepository.homeFilters())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { filters ->
                        if (filters.isEmpty()) {
                            hideFilters()
                        } else {
                            setFilters(filters)
                        }
                    },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            filterManager.asFlowable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { *//*filterManager -> view.updateFilters(filterManager.totalFilters)*//* },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            filterManager.periodRequest
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { *//*periodRequest -> view.showPeriodRequest(periodRequest.first)*//* },
                    { Timber.e(it) }
                )
        )
    }*/

    fun onSyncAllClick() {
        Timber.tag("SYNC").e("WORKS")
    }

    fun showFilter() {
        Timber.tag("FILTER").e("WORKS")
    }
}