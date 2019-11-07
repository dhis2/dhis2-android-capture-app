package org.dhis2.usecases.datasets.dataSetInitial

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract
import org.dhis2.usescases.datasets.dataSetTable.DataSetTablePresenter
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableRepository
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialContract
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialPresenter
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepository
import org.dhis2.usescases.datasets.datasetInitial.DateRangeInputPeriodModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataInputPeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Date

class DataSetInitialPresenterTest {

    private lateinit var presenter: DataSetInitialPresenter

    private val view: DataSetInitialContract.View = mock()
    private val repository: DataSetInitialRepository = mock()
    private val scheduler = TrampolineSchedulerProvider()

    @Before
    fun setUp() {
        presenter = DataSetInitialPresenter(view, repository, scheduler)
    }

    @Test
    fun `Should go back when bakc button is clicked`(){
        presenter.onBackClick()

        verify(view).back()
    }

    @Test
    fun `Should dispose of all disposables`(){
        presenter.onDettach()

        val disposableSize = presenter.compositeDisposable.size()

        Assert.assertTrue(disposableSize == 0)
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }

    @Test
    fun `Should show orgUnitDialog when field is clicked`() {
        val orgUnits = listOf<OrganisationUnit>()

        presenter.onOrgUnitSelectorClick()

        verify(view).showOrgUnitDialog(orgUnits)
    }

    @Test
    fun `Should show periodSelector when field is clicked`() {
        val periodType = PeriodType.Monthly
        val periods = listOf(DateRangeInputPeriodModel.create("dataSet", "period", Date(), Date(), Date(), Date()))

        whenever(repository.dataInputPeriod) doReturn Flowable.just(periods)

        presenter.onReportPeriodClick(PeriodType.Monthly)

        verify(view).showPeriodSelector(periodType, periods, 0)
    }


}