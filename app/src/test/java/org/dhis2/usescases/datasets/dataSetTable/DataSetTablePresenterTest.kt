package org.dhis2.usescases.datasets.dataSetTable

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSection
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.validationrules.ValidationRuleResult
import org.dhis2.utils.validationrules.Violation
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataelement.DataElementOperand
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.validation.engine.ValidationResult
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.anyArray
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class DataSetTablePresenterTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var presenter: DataSetTablePresenter

    private val view: DataSetTableContract.View = mock()
    private val repository: DataSetTableRepositoryImpl = mock()
    private val periodUtils: DhisPeriodUtils = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val updateProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    private val testingDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        presenter = DataSetTablePresenter(
            view,
            repository,
            periodUtils,
            object : DispatcherProvider {
                override fun io(): CoroutineDispatcher {
                    return testingDispatcher
                }

                override fun computation(): CoroutineDispatcher {
                    return testingDispatcher
                }

                override fun ui(): CoroutineDispatcher {
                    return testingDispatcher
                }
            },
            analyticsHelper,
            updateProcessor,
            false
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Should initialize the DataSet table presenter`() {
        val sections = listOf(DataSetSection("section_1_uid", "section_1"))
        val dataSet = DataSet.builder().uid("datasetUid").displayName("name").build()
        val catComboName = "catComboName"
        val period = Period.builder().periodType(PeriodType.Daily)
            .startDate(Date())
            .endDate(Date())
            .periodId("periodId")
            .build()

        val orgUnit: OrganisationUnit = mock {
            on { displayName() } doReturn "orgUnitName"
        }

        val renderDetails = DataSetRenderDetails(
            dataSet.displayName()!!,
            "orgUnitName",
            "periodLabel",
            catComboName,
            false
        )

        whenever(repository.getSections()) doReturn Flowable.just(sections)
        whenever(repository.getDataSet()) doReturn Single.just(dataSet)
        whenever(repository.getCatComboName()) doReturn Flowable.just(catComboName)
        whenever(repository.getPeriod()) doReturn Single.just(period)
        whenever(
            periodUtils.getPeriodUIString(
                period.periodType(),
                period.startDate()!!,
                Locale.getDefault()
            )
        ) doReturn "periodLabel"
        whenever(repository.getOrgUnit()) doReturn Single.just(orgUnit)
        whenever(repository.isComplete()) doReturn Single.just(false)
        whenever(repository.dataSetStatus()) doReturn Flowable.just(true)
        whenever(repository.dataSetState()) doReturn Flowable.just(State.SYNCED)

        testingDispatcher.scheduler.advanceUntilIdle()

        val result = presenter.dataSetScreenState.value
        assertTrue(result.renderDetails == renderDetails)
    }

    @Test
    fun `Should show success if no validation rules exist`() {
        whenever(view.isErrorBottomSheetShowing) doReturn false
        whenever(repository.hasValidationRules()) doReturn false
        whenever(repository.isComplete()) doReturn Single.just(false)
        presenter.handleSaveClick()
        verify(view).showSuccessValidationDialog()
    }

    @Test
    fun `Should run validations`() {
        whenever(view.isErrorBottomSheetShowing) doReturn false
        whenever(repository.hasValidationRules()) doReturn true
        whenever(repository.areValidationRulesMandatory()) doReturn true

        presenter.handleSaveClick()
        testingDispatcher.scheduler.advanceUntilIdle()
        verify(repository).executeValidationRules()
    }

    @Test
    fun `Should re-run validations when a rule was fixed`() {
        whenever(view.isErrorBottomSheetShowing) doReturn true
        whenever(repository.hasValidationRules()) doReturn true
        whenever(repository.areValidationRulesMandatory()) doReturn true
        presenter.handleSaveClick()
        testingDispatcher.scheduler.advanceUntilIdle()
        verify(view).closeBottomSheet()
        verify(repository).executeValidationRules()
    }

    @Test
    fun `Should show success dialog`() {
        whenever(repository.isComplete()) doReturn Single.just(false)
        presenter.handleValidationResult(
            ValidationRuleResult(
                ValidationResult.ValidationResultStatus.OK,
                emptyList()
            )
        )
        verify(view).showSuccessValidationDialog()
    }

    @Test
    fun `Should save and finish`() {
        whenever(repository.isComplete()) doReturn Single.just(true)
        presenter.handleValidationResult(
            ValidationRuleResult(
                ValidationResult.ValidationResultStatus.OK,
                emptyList()
            )
        )
        verify(view).saveAndFinish()
    }

    @Test
    fun `Should show validation error dialog`() {
        val violations = anyArray<Violation>().toList()
        presenter.handleValidationResult(
            ValidationRuleResult(
                ValidationResult.ValidationResultStatus.ERROR,
                violations
            )
        )
        verify(view).showErrorsValidationDialog(violations)
    }

    @Test
    fun `Should show validation dialog if validation rules exist and are not mandatory`() {
        whenever(repository.hasValidationRules()) doReturn true
        whenever(repository.areValidationRulesMandatory()) doReturn false
        presenter.handleSaveClick()
        verify(view).showValidationRuleDialog()
    }

    @Test
    fun `Should go back when back button is clicked`() {
        presenter.onBackClick()

        verify(view).back()
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }

    @Test
    fun `Should show the mark as complete snackbar if dataset was not previously completed`() {
        whenever(repository.checkMandatoryFields()) doReturn Single.just(emptyList())
        whenever(repository.checkFieldCombination()) doReturn Single.just(
            Pair.create(
                true,
                emptyList()
            )
        )
        whenever(repository.completeDataSetInstance()) doReturn Single.just(false)
        presenter.completeDataSet()
        testingDispatcher.scheduler.advanceUntilIdle()
        verify(view).savedAndCompleteMessage()
    }

    @Test
    fun `Should show missing mandatory fields`() {
        whenever(
            repository.checkMandatoryFields()
        ) doReturn Single.just(
            arrayListOf(
                DataElementOperand.builder()
                    .uid("uid")
                    .build()
            ).toList()

        )
        whenever(
            repository.checkFieldCombination()
        ) doReturn Single.just(
            Pair.create(
                true,
                emptyList()
            )
        )
        whenever(repository.completeDataSetInstance()) doReturn Single.just(false)
        presenter.completeDataSet()
        testingDispatcher.scheduler.advanceUntilIdle()
        verify(view).showMandatoryMessage(true)
    }

    @Test
    fun `Should show combination fields message`() {
        whenever(
            repository.checkMandatoryFields()
        ) doReturn Single.just(emptyList())
        whenever(
            repository.checkFieldCombination()
        ) doReturn Single.just(
            Pair.create(
                false,
                emptyList()
            )
        )
        whenever(repository.completeDataSetInstance()) doReturn Single.just(false)
        presenter.completeDataSet()
        testingDispatcher.scheduler.advanceUntilIdle()
        verify(view).showMandatoryMessage(false)
    }

    @Test
    fun `Should finish with message`() {
        whenever(repository.checkMandatoryFields()) doReturn Single.just(emptyList())
        whenever(repository.checkFieldCombination()) doReturn Single.just(
            Pair.create(
                true,
                emptyList()
            )
        )
        whenever(repository.completeDataSetInstance()) doReturn Single.just(true)
        presenter.completeDataSet()
        testingDispatcher.scheduler.advanceUntilIdle()
        verify(view).saveAndFinish()
    }

    @Test
    fun `Should close or expand the bottom sheet`() {
        presenter.collapseExpandBottomSheet()

        verify(view).collapseExpandBottom()
    }

    @Test
    fun `Should close bottom sheet on cancel click`() {
        presenter.closeBottomSheet()

        verify(view).closeBottomSheet()
    }

    @Test
    fun `Should close bottom sheet on complete click`() {
        presenter.onCompleteBottomSheet()

        verify(view).completeBottomSheet()
    }
}
