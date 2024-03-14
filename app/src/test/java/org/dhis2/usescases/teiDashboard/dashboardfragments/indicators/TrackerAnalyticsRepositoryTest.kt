package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.ui.SectionTitle
import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.android.core.program.ProgramRule
import org.hisp.dhis.android.core.program.ProgramRuleAction
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TrackerAnalyticsRepositoryTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val ruleEngine: RuleEngine = mock()
    private val ruleEngineRepository: RuleEngineRepository = mock()
    private val charts: Charts = mock()
    private val resourceManager: ResourceManager = mock()
    private val enrollmentRepository: EnrollmentCollectionRepository = mock()
    private lateinit var repository: IndicatorRepository

    @Before
    fun setUp() {
        whenever(
            d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq("teiUid"),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq("teiUid")
                .byProgram(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq("teiUid")
                .byProgram().eq("programUid"),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq("teiUid")
                .byProgram().eq("programUid").one(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq("teiUid")
                .byProgram().eq("programUid").one().blockingGet(),
        ) doReturn Enrollment.builder().uid("enrollmentUid").build()
        whenever(
            resourceManager.sectionCharts(),
        ) doReturn "Charts"
        whenever(
            resourceManager.sectionIndicators(),
        ) doReturn "Indicators"
        whenever(
            resourceManager.sectionFeedback(),
        ) doReturn "Feedback"
        whenever(
            resourceManager.sectionChartsAndIndicators(),
        ) doReturn "Charts and indicators"
        whenever(
            resourceManager.defaultIndicatorLabel(),
        ) doReturn "Info"
        repository = TrackerAnalyticsRepository(
            d2,
            ruleEngineRepository,
            charts,
            "programUid",
            "teiUid",
            resourceManager,
        )
    }

    @Test
    fun `Should fetch analytic data for tracker`() {
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue,
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid(),
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid().eq("programUid"),
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid().eq("programUid")
                .withLegendSets(),
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid().eq("programUid")
                .withLegendSets()
                .get(),
        ) doReturn Single.just(mockedProgramIndicatorList())

        whenever(
            d2.programModule().programIndicatorEngine(),
        ) doReturn mock()

        whenever(
            d2.programModule().programIndicatorEngine().getEnrollmentProgramIndicatorValue(
                "enrollmentUid",
                "programIndicatorUid_1",
            ),
        ) doReturn "1.0"

        whenever(
            d2.programModule().programRules()
                .byProgramUid().eq("programUid"),
        ) doReturn mock()
        whenever(
            d2.programModule().programRules()
                .byProgramUid().eq("programUid")
                .get(),
        ) doReturn Single.just(mockedRules())
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() }),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() })
                .byProgramRuleActionType(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() })
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                ),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() })
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                )
                .get(),
        ) doReturn Single.just(mockedActions())
        whenever(
            ruleEngineRepository.updateRuleEngine(),
        ) doReturn Flowable.just(ruleEngine)
        whenever(
            ruleEngineRepository.reCalculate(),
        ) doReturn Flowable.just(Result.success(mockedEffects()))

        whenever(
            charts.geEnrollmentCharts(any()),
        ) doReturn mockedCharts()

        val testObserver = repository.fetchData().test()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 7 &&
                it[0] is SectionTitle && (it[0] as SectionTitle).title == "Feedback" &&
                it[2] is SectionTitle && (it[2] as SectionTitle).title == "Charts and indicators"
        }
    }

    @Test
    fun `Should fetch analytic data for tracker with only indicator section`() {
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue,
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid(),
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid().eq("programUid"),
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid().eq("programUid")
                .withLegendSets(),
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid().eq("programUid")
                .withLegendSets()
                .get(),
        ) doReturn Single.just(mockedProgramIndicatorList())

        whenever(
            d2.programModule().programIndicatorEngine(),
        ) doReturn mock()

        whenever(
            d2.programModule().programIndicatorEngine().getEnrollmentProgramIndicatorValue(
                "enrollmentUid",
                "programIndicatorUid_1",
            ),
        ) doReturn "1.0"

        whenever(
            d2.programModule().programRules()
                .byProgramUid().eq("programUid"),
        ) doReturn mock()
        whenever(
            d2.programModule().programRules()
                .byProgramUid().eq("programUid")
                .get(),
        ) doReturn Single.just(mockedRules())
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() }),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() })
                .byProgramRuleActionType(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() })
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                ),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() })
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                )
                .get(),
        ) doReturn Single.just(mockedActions())
        whenever(
            ruleEngineRepository.updateRuleEngine(),
        ) doReturn Flowable.just(ruleEngine)
        whenever(
            ruleEngineRepository.reCalculate(),
        ) doReturn Flowable.just(Result.success(emptyList()))

        whenever(
            charts.geEnrollmentCharts(any()),
        ) doReturn emptyList()

        val testObserver = repository.fetchData().test()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it[0] is SectionTitle && (it[0] as SectionTitle).title == "Indicators"
        }
    }

    @Test
    fun `Should fetch analytic data for tracker with only charts section`() {
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue,
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid(),
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid().eq("programUid"),
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid().eq("programUid")
                .withLegendSets(),
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid().eq("programUid")
                .withLegendSets()
                .get(),
        ) doReturn Single.just(emptyList())

        whenever(
            d2.programModule().programIndicatorEngine(),
        ) doReturn mock()

        whenever(
            d2.programModule().programIndicatorEngine().getEnrollmentProgramIndicatorValue(
                "enrollmentUid",
                "programIndicatorUid_1",
            ),
        ) doReturn "1.0"

        whenever(
            d2.programModule().programRules()
                .byProgramUid().eq("programUid"),
        ) doReturn mock()
        whenever(
            d2.programModule().programRules()
                .byProgramUid().eq("programUid")
                .get(),
        ) doReturn Single.just(mockedRules())
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() }),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() })
                .byProgramRuleActionType(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() })
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                ),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRules().map { it.uid() })
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                )
                .get(),
        ) doReturn Single.just(mockedActions())
        whenever(
            ruleEngineRepository.updateRuleEngine(),
        ) doReturn Flowable.just(ruleEngine)
        whenever(
            ruleEngineRepository.reCalculate(),
        ) doReturn Flowable.just(Result.success(emptyList()))

        whenever(
            charts.geEnrollmentCharts(any()),
        ) doReturn mockedCharts()

        val testObserver = repository.fetchData().test()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it[0] is SectionTitle && (it[0] as SectionTitle).title == "Charts"
        }
    }

    private fun mockedProgramIndicatorList(): List<ProgramIndicator> {
        return listOf(
            ProgramIndicator.builder()
                .uid("programIndicatorUid_1")
                .displayInForm(true)
                .build(),
            ProgramIndicator.builder()
                .uid("programIndicatorUid_2")
                .displayInForm(false)
                .build(),
        )
    }

    private fun mockedRules(): List<ProgramRule> {
        return listOf(
            ProgramRule.builder().uid("rule_1").build(),
        )
    }

    private fun mockedActions(): List<ProgramRuleAction> {
        return listOf(
            ProgramRuleAction.builder().uid("rule_action_2").build(),
        )
    }

    private fun mockedEffects(): List<RuleEffect> {
        return listOf(
            RuleEffect.create(
                "ruleUid1",
                RuleActionDisplayKeyValuePair.createForFeedback("content", "data"),
                "data",
            ),
            RuleEffect.create(
                "ruleUid2",
                RuleActionDisplayKeyValuePair.createForIndicators("content", "data"),
                "data",
            ),
        )
    }

    private fun mockedCharts(): List<Graph> {
        return listOf(
            Graph(
                "title",
                emptyList(),
                RelativePeriod.LAST_5_YEARS,
                PeriodType.Yearly,
                10,
            ),
        )
    }
}
