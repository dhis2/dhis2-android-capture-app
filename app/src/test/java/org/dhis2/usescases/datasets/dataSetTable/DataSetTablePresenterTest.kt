package org.dhis2.usescases.datasets.dataSetTable

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Date

class DataSetTablePresenterTest {

    private lateinit var presenter: DataSetTablePresenter

    private val view: DataSetTableContract.View = mock()
    private val repository: DataSetTableRepository = mock()
    private val scheduler = TrampolineSchedulerProvider()
    private val analyticsHelper: AnalyticsHelper = mock()

    @Before
    fun setUp() {
        presenter = DataSetTablePresenter(view, repository, scheduler, analyticsHelper)
    }

    @Test
    fun `Should initialize the DataSet table presenter`() {
        val orgUnit = "orgUnitUid"
        val periodTypeName = "daily"
        val catCombo = "catComboUid"
        val periodFinalDate = "12/05/2018"
        val periodId = "periodId"

        val sections = listOf("section_1")
        val dataSet = DataSet.builder().uid("datasetUid").build()
        val catComboName = "catComboName"
        val period = Period.builder().periodType(PeriodType.Daily)
            .startDate(Date())
            .endDate(Date())
            .periodId("periodId")
            .build()


        whenever(repository.sections) doReturn Flowable.just(sections)
        whenever(repository.dataSet) doReturn Flowable.just(dataSet)
        whenever(repository.getCatComboName(catCombo)) doReturn Flowable.just(catComboName)
        whenever(repository.period) doReturn Flowable.just(period)
        whenever(repository.dataSetStatus()) doReturn Flowable.just(true)
        whenever(repository.dataSetState()) doReturn Flowable.just(State.SYNCED)

        presenter.init(orgUnit, periodTypeName, catCombo, periodFinalDate, periodId)

        verify(view).setSections(sections)
        verify(view).renderDetails(dataSet, catComboName, period)
    }

    @Test
    fun `Should go back when bakc button is clicked`() {
        presenter.onBackClick()

        verify(view).back()
    }

    @Test
    fun `Should dispose of all disposables`() {
        presenter.onDettach()

        val disposableSize = presenter.disposable.size()

        Assert.assertTrue(disposableSize == 0)
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }
}
