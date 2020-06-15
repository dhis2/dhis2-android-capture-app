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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.FilterManager.PeriodRequest
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.junit.Before
import org.junit.Test

class DataSetDetailPresenterTest {

    private lateinit var presenter: DataSetDetailPresenter

    private val view: DataSetDetailView = mock()
    private val repository: DataSetDetailRepository = mock()
    private val scheduler = TestSchedulerProvider(TestScheduler())
    private val filterManager: FilterManager = mock()

    @Before
    fun setUp() {
        presenter = DataSetDetailPresenter(view, repository, scheduler, filterManager)
    }

    @Test
    fun `Should get the list of dataSet`() {
        val filterProcessor: FlowableProcessor<FilterManager> = PublishProcessor.create()
        val periodRequest: FlowableProcessor<PeriodRequest> = BehaviorProcessor.create()
        val filterManagerFlowable = Flowable.just(filterManager).startWith(filterProcessor)
        val dataSets = listOf(dummyDataSet(), dummyDataSet(), dummyDataSet())
        val catOptionComboPair = Pair.create(dummyCategoryCombo(), dummyListCatOptionCombo())

        whenever(filterManager.asFlowable()) doReturn filterManagerFlowable
        whenever(filterManager.ouTreeFlowable()) doReturn Flowable.just(true)
        whenever(
            repository.dataSetGroups(any(), any(), any(), any())
        ) doReturn Flowable.just(dataSets)
        whenever(filterManager.periodRequest) doReturn periodRequest
        whenever(repository.catOptionCombos()) doReturn Single.just(catOptionComboPair)
        whenever(repository.canWriteAny()) doReturn Flowable.just(true)
        filterProcessor.onNext(filterManager)
        periodRequest.onNext(PeriodRequest.FROM_TO)

        presenter.init()
        scheduler.io().triggerActions()

        verify(view).openOrgUnitTreeSelector()
        verify(view).setData(dataSets)
        verify(view).updateFilters(any())
        verify(view).showPeriodRequest(periodRequest.blockingFirst())
        verify(view).setCatOptionComboFilter(catOptionComboPair)
        verify(view).setWritePermission(true)
    }

    @Test
    fun `Should navigate to activity to add a new DataSer`() {
        presenter.addDataSet()

        verify(view).startNewDataSet()
    }

    @Test
    fun `Should go back when back button is pressed`() {
        presenter.onBackClick()

        verify(view).back()
    }

    @Test
    fun `Should open a dataSet`() {
        val dataSet = dummyDataSet()

        presenter.openDataSet(dataSet)

        verify(view).openDataSet(dataSet)
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
    fun `Should show sync dialog`() {
        val dataSet = dummyDataSet()

        presenter.onSyncIconClick(dataSet)

        verify(view).showSyncDialog(dataSet)
    }

    @Test
    fun `Should publish filter data`() {
        presenter.updateFilters()

        verify(filterManager).publishData()
    }

    @Test
    fun `Should clear all filters when reset filter button is clicked`() {
        presenter.clearFilterClick()

        verify(filterManager).clearAllFilters()
        verify(view).clearFilters()
    }

    private fun dummyDataSet() = DataSetDetailModel.create(
        "",
        "",
        "",
        "",
        "",
        "",
        State.SYNCED,
        ""
    )

    private fun dummyCategoryCombo() = CategoryCombo.builder().uid("uid").build()

    private fun dummyListCatOptionCombo(): List<CategoryOptionCombo> {
        return listOf(CategoryOptionCombo.builder().uid("uid").build())
    }
}
