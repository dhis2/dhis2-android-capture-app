package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.ui.SectionTitle
import io.reactivex.Single
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.android.core.program.ProgramRuleAction
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.models.RuleAction
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
    private val ruleEngineHelper: RuleEngineHelper = mock()
    private val charts: Charts = mock()
    private val resourceManager: ResourceManager = mock()
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
            ruleEngineHelper,
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
                .getUids(),
        ) doReturn Single.just(mockedRuleUids())
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids()),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids())
                .byProgramRuleActionType(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids())
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                ),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids())
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                )
                .get(),
        ) doReturn Single.just(mockedActions())
        whenever(
            ruleEngineHelper.evaluate(),
        ) doReturn mockedEffects()

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
                .getUids(),
        ) doReturn Single.just(mockedRuleUids())
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids()),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids())
                .byProgramRuleActionType(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids())
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                ),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids())
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                )
                .get(),
        ) doReturn Single.just(mockedActions())
        whenever(
            ruleEngineHelper.evaluate(),
        ) doReturn emptyList()

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
                .getUids(),
        ) doReturn Single.just(mockedRuleUids())
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids()),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids())
                .byProgramRuleActionType(),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids())
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                ),
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(mockedRuleUids())
                .byProgramRuleActionType().`in`(
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR,
                    ProgramRuleActionType.DISPLAYTEXT,
                )
                .get(),
        ) doReturn Single.just(mockedActions())

        whenever(
            ruleEngineHelper.evaluate(),
        ) doReturn emptyList()

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

    private fun mockedRuleUids(): List<String> {
        return listOf("rule_1")
    }

    private fun mockedActions(): List<ProgramRuleAction> {
        return listOf(
            ProgramRuleAction.builder().uid("rule_action_2").build(),
        )
    }

    private fun mockedEffects(): List<RuleEffect> {
        return listOf(
            RuleEffect(
                "ruleUid1",
                RuleAction(
                    data = "data",
                    type = ProgramRuleActionType.DISPLAYKEYVALUEPAIR.name,
                    values = mutableMapOf(
                        Pair("content", "content"),
                        Pair("location", "feedback"),
                    ),
                ),
            ),
            RuleEffect(
                "ruleUid2",
                RuleAction(
                    data = "data",
                    type = ProgramRuleActionType.DISPLAYKEYVALUEPAIR.name,
                    values = mutableMapOf(
                        Pair("content", "content"),
                        Pair("location", "indicators"),
                    ),
                ),
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
