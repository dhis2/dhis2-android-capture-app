/*
* Copyright (c) 2004-2019, University of Oslo
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright notice, this
* list of conditions and the following disclaimer.
*
* Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.
* Neither the name of the HISP project nor the names of its contributors may
* be used to endorse or promote products derived from this software without
* specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
* ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.dhis2.usescases.datasets.datasetDetail

import androidx.annotation.VisibleForTesting
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.filters.FilterManager
import timber.log.Timber

class DataSetDetailPresenter internal constructor(
    private val view: DataSetDetailView,
    private val dataSetDetailRepository: DataSetDetailRepository,
    private val schedulerProvider: SchedulerProvider,
    private val filterManager: FilterManager
) {
    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        getOrgUnits()

        disposable.add(
            filterManager.asFlowable()
                .startWith(FilterManager.getInstance())
                .flatMap { filterManager ->
                    dataSetDetailRepository.dataSetGroups(
                        filterManager.orgUnitUidsFilters,
                        filterManager.periodFilters,
                        filterManager.stateFilters,
                        filterManager.catOptComboFilters
                    )
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { data ->
                        view.setData(data)
                        view.updateFilters(filterManager.totalFilters)
                    },
                    { Timber.d(it) }
                )
        )

        disposable.add(
            filterManager.periodRequest
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { periodRequest -> view.showPeriodRequest(periodRequest) },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            dataSetDetailRepository.catOptionCombos()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setCatOptionComboFilter(it) },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            dataSetDetailRepository.canWriteAny()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setWritePermission(it) },
                    { Timber.e(it) }
                )
        )
    }

    fun addDataSet() {
        view.startNewDataSet()
    }

    fun onBackClick() {
        view.back()
    }

    fun openDataSet(dataSet: DataSetDetailModel) {
        view.openDataSet(dataSet)
    }

    fun showFilter() {
        view.showHideFilter()
    }

    @VisibleForTesting
    fun getOrgUnits() {
        disposable.add(
            filterManager.ouTreeFlowable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.openOrgUnitTreeSelector() },
                    { Timber.e(it) }
                )
        )
    }

    fun onDettach() {
        disposable.clear()
    }

    fun displayMessage(message: String) {
        view.displayMessage(message)
    }

    fun onSyncIconClick(dataSet: DataSetDetailModel) {
        view.showSyncDialog(dataSet)
    }

    fun updateFilters() {
        filterManager.publishData()
    }

    fun clearFilterClick() {
        filterManager.clearAllFilters()
        view.clearFilters()
    }
}
