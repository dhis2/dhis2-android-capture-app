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

import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FilterManager.PeriodRequest
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DataSetDetailPresenterTest {

    private lateinit var presenter: DataSetDetailPresenter

    private val view: DataSetDetailView = mock()
    private val repository: DataSetDetailRepository = mock()
    private val scheduler = TestSchedulerProvider(TestScheduler())
    private val filterManager: FilterManager = mock()
    private val filterRepository: FilterRepository = mock()
    private val disableHomeFilters: DisableHomeFiltersFromSettingsApp = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()

    @Before
    fun setUp() {
        presenter = DataSetDetailPresenter(
            view,
            repository,
            scheduler,
            filterManager,
            filterRepository,
            disableHomeFilters,
            matomoAnalyticsController,
        )
    }

    @Test
    fun `Should init filters`() {
        val filterProcessor: FlowableProcessor<FilterManager> = PublishProcessor.create()
        val periodRequest: FlowableProcessor<kotlin.Pair<PeriodRequest, Filters?>> =
            BehaviorProcessor.create()
        val filterManagerFlowable = Flowable.just(filterManager).startWith(filterProcessor)
        val catOptionComboPair = Pair.create(dummyCategoryCombo(), dummyListCatOptionCombo())

        whenever(filterManager.asFlowable()) doReturn filterManagerFlowable
        whenever(filterManager.ouTreeFlowable()) doReturn Flowable.just(true)
        whenever(filterManager.periodRequest) doReturn periodRequest
        filterProcessor.onNext(filterManager)
        periodRequest.onNext(Pair(PeriodRequest.FROM_TO, null))

        presenter.init()
        scheduler.io().triggerActions()

        verify(view).openOrgUnitTreeSelector()
        verify(view).updateFilters(any())
        verify(view).showPeriodRequest(periodRequest.blockingFirst().first)
    }

    @Test
    fun `Should go back when back button is pressed`() {
        presenter.onBackClick()

        verify(view).back()
    }

    @Test
    fun `Should show filters section`() {
        presenter.showFilter()

        verify(view).showHideFilter()
    }

    @Test
    fun `Should open the orgUnit tree selector`() {
        whenever(filterManager.ouTreeFlowable()) doReturn Flowable.just(true)

        presenter.getOrgUnits()
        scheduler.io().triggerActions()

        verify(view).openOrgUnitTreeSelector()
    }

    @Test
    fun `Should dispose of all disposables`() {
        presenter.onDettach()

        val result = presenter.disposable.size()

        assert(result == 0)
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }

    @Test
    fun `Should clear all filters when reset filter button is clicked`() {
        presenter.clearFilterClick()

        verify(filterManager).clearAllFilters()
        verify(view).clearFilters()
    }

    private fun dummyCategoryCombo() = CategoryCombo.builder().uid("uid").build()

    private fun dummyListCatOptionCombo(): List<CategoryOptionCombo> {
        return listOf(CategoryOptionCombo.builder().uid("uid").build())
    }
}
