package org.dhis2.usescases.datasets.dataSetTable

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.validation.engine.ValidationResult
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DataSetTablePresenterTest {

    private lateinit var presenter: DataSetTablePresenter

    private val view: DataSetTableContract.View = mock()
    private val repository: DataSetTableRepositoryImpl = mock()
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

        whenever(repository.getSections()) doReturn Flowable.just(sections)
        whenever(repository.getDataSet()) doReturn Single.just(dataSet)
        whenever(repository.getCatComboName(catCombo)) doReturn Flowable.just(catComboName)
        whenever(repository.getPeriod()) doReturn Single.just(period)
        whenever(repository.dataSetStatus()) doReturn Flowable.just(true)
        whenever(repository.dataSetState()) doReturn Flowable.just(State.SYNCED)
        whenever(view.observeSaveButtonClicks()) doReturn Observable.empty()

        presenter.init(orgUnit, periodTypeName, catCombo, periodFinalDate, periodId)

        verify(view).setSections(sections)
        verify(view).renderDetails(dataSet, catComboName, period)
    }

    @Test
    fun `Should go back when back button is clicked`() {
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

    @Test
    fun `Should check if DataSet does have ValidationRules and show appropriate dialog`() {
        whenever(repository.doesDatasetHasValidationRulesAssociated()) doReturn true

        presenter.checkIfValidationRulesExecutionIsOptional()

        verify(view).showValidationRuleDialog()
    }

    @Test
    fun `Should check if DataSet does not have ValidationRules associated and complete DataSet`() {
        whenever(repository.doesDatasetHasValidationRulesAssociated()) doReturn false
        whenever(repository.completeDataSetInstance()) doReturn Completable.complete()

        presenter.checkIfValidationRulesExecutionIsOptional()

        verifyZeroInteractions(view)
    }

    @Test
    fun `Should execute ValidationRules without errors`() {
        val resultOk =
            ValidationResult.builder()
                .status(ValidationResult.ValidationResultStatus.OK)
                .violations(emptyList()).build()

        whenever(repository.executeValidationRules()) doReturn Flowable.just(resultOk)

        presenter.executeValidationRules()

        verify(view).showSuccessValidationDialog()
    }

    @Test
    fun `Should execute ValidationRules and the result is with errors`() {
        val resultError =
            ValidationResult.builder()
                .status(ValidationResult.ValidationResultStatus.ERROR)
                .violations(emptyList()).build()

        whenever(repository.executeValidationRules()) doReturn Flowable.just(resultError)

        presenter.executeValidationRules()

        verify(view).showErrorsValidationDialog()
    }

    @Test
    fun `Should mark the dataset as complete`() {
        whenever(repository.completeDataSetInstance()) doReturn Completable.complete()

        presenter.completeDataSet()

        verifyZeroInteractions(view)
    }
}
