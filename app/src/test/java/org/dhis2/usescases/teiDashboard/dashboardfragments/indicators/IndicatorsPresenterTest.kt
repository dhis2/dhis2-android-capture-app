package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.rules.RuleEngineContext
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleActionDisplayText
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleValueType
import org.hisp.dhis.rules.models.RuleVariableNewestEvent
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class IndicatorsPresenterTest {

    private val dashboardRepository: DashboardRepository = mock()
    private val ruleEngineRepository: RuleEngineRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val view: IndicatorsView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private lateinit var presenter: IndicatorsPresenterImpl

    @Before
    fun setUp(){
        whenever(d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq("tei_uid")) doReturn mock()

        whenever(d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq("tei_uid").one()) doReturn mock()
        whenever(d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq("tei_uid").one()
            .blockingGet()) doReturn Enrollment.builder().uid("enrollment_uid").build()

        whenever(d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq("tei_uid")
            .byProgram()) doReturn mock()

        whenever(d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq("tei_uid")
            .byProgram().eq("program_uid")) doReturn mock()

        whenever(d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq("tei_uid")
            .byProgram().eq("program_uid").one()) doReturn mock()

        whenever(d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq("tei_uid")
            .byProgram().eq("program_uid").one()
            .blockingGet()) doReturn Enrollment.builder().uid("enrollment_uid").build()

        presenter = IndicatorsPresenterImpl(d2, "program_uid", "tei_uid",
            dashboardRepository, ruleEngineRepository, schedulers, view)
    }

    @Test
    fun `Should get indicators`(){
        whenever(dashboardRepository
            .getIndicators("program_uid")) doReturn indicatorsFlowable()

        whenever(d2.programModule().programIndicatorEngine()
            .getProgramIndicatorValue(
                "enrollment_uid",
                null,
                "indicator_uid")) doReturn "indicator_value"

        whenever(dashboardRepository
            .getLegendColorForIndicator(
                indicators()[0],
                "indicator_value")) doReturn Observable.just(
                        Trio.create(indicators()[0], "indicator_value", "0"))

        whenever(ruleEngineRepository.updateRuleEngine()) doReturn ruleEngine()

        whenever(ruleEngineRepository.reCalculate()) doReturn resultRuleEffect()

        presenter.init()

        verify(view).swapIndicators(any())
    }

    @Test
    fun `Should clear disposables`() {
        presenter.onDettach()

        Assert.assertTrue(presenter.compositeDisposable.size() == 0)
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }

    private fun indicatorsFlowable() =
        Flowable.just(listOf(ProgramIndicator.builder().uid("indicator_uid")
            .displayInForm(true).build()))

    private fun indicators() =
        listOf(ProgramIndicator.builder().uid("indicator_uid")
            .displayInForm(true).build())

    private fun ruleEngine() =
        Flowable.just(RuleEngineContext.builder {"expression"}
            .rules(listOf(Rule.create(
                "programstage", 0,"condition",
                listOf(RuleActionDisplayText.createForIndicators("content", "data")),
                "name")))
            .ruleVariables(listOf(RuleVariableNewestEvent.create("name", "dataElement",
                RuleValueType.TEXT)))
            .calculatedValueMap(HashMap())
            .supplementaryData(hashMapOf(Pair("key", listOf("data"))))
            .constantsValue(hashMapOf(Pair("key", "constant")))
            .build().toEngineBuilder().build())

    private fun resultRuleEffect() =
        Flowable.just(Result.success(listOf(RuleEffect.create(
            RuleActionDisplayText.createForIndicators("content", "data")))))

}