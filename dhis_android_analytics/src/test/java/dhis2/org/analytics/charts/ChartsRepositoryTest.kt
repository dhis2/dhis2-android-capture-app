package dhis2.org.analytics.charts

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.mappers.AnalyticsTeiSettingsToGraph
import dhis2.org.analytics.charts.mappers.DataElementToGraph
import dhis2.org.analytics.charts.mappers.ProgramIndicatorToGraph
import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.hisp.dhis.android.core.settings.AnalyticsTeiData
import org.hisp.dhis.android.core.settings.AnalyticsTeiSetting
import org.hisp.dhis.android.core.settings.ChartType
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class ChartsRepositoryTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val analyticsTeiSettingsToGraph: AnalyticsTeiSettingsToGraph = mock()
    private val dataElementToGraph: DataElementToGraph = mock()
    private val programIndicatorToGraph: ProgramIndicatorToGraph = mock()
    private val repository = ChartsRepositoryImpl(
        d2,
        analyticsTeiSettingsToGraph,
        dataElementToGraph,
        programIndicatorToGraph
    )

    @Test
    fun `Should return empty list if enrollment teiUid is null`() {
        whenever(
            d2.enrollmentModule()
                .enrollments()
                .uid(any())
                .blockingGet()
        ) doReturn Enrollment.builder()
            .uid("enrollmentUid")
            .program("programUid")
            .trackedEntityInstance(null)
            .build()
        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(
            result.isEmpty()
        )
    }

    @Test
    fun `Should get analytics if settings is not null`() {
        mockEnrollmentCall()
        mockAnalyticsSettingsCall(mockedAnalyticsSettings())
        whenever(
            analyticsTeiSettingsToGraph.map(any(), any(), any(), any(), any())
        ) doReturn mockedSettingsGraphs()

        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(
            result.isNotEmpty() &&
                result.size == mockedSettingsGraphs().size &&
                result[0].title == "settings_1"
        )
    }

    @Test
    fun `Should get default analytics if settings is null`() {
        mockEnrollmentCall()
        mockAnalyticsSettingsCall(null)
        mockRepeatableStagesCall()
        mockNumericDataElements(false)
        whenever(
            dataElementToGraph.map(any(), any(), any(), any())
        ) doReturn mockedDataElementGraph()
        mockIndicators(false)
        whenever(
            programIndicatorToGraph.map(any(), any(), any(), any())
        ) doReturn mockedIndicatorGraph()
        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(
            result.isNotEmpty() &&
                result.size == 2 &&
                result[0].title == "de_graph_1" &&
                result[1].title == "indicator_graph_1"
        )
    }

    @Test
    fun `Should get default analytics if settings is null and return only dataElement graphs`() {
        mockEnrollmentCall()
        mockAnalyticsSettingsCall(null)
        mockRepeatableStagesCall()
        mockNumericDataElements(false)
        whenever(
            dataElementToGraph.map(any(), any(), any(), any())
        ) doReturn mockedDataElementGraph()
        mockIndicators(true)
        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(
            result.isNotEmpty() &&
                result.size == 1 &&
                result[0].title == "de_graph_1"
        )
    }

    @Test
    fun `Should get default analytics if settings is null and return only indicator graphs`() {
        mockEnrollmentCall()
        mockAnalyticsSettingsCall(null)
        mockRepeatableStagesCall()
        mockNumericDataElements(true)
        mockIndicators(false)
        whenever(
            programIndicatorToGraph.map(any(), any(), any(), any())
        ) doReturn mockedIndicatorGraph()
        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(
            result.isNotEmpty() &&
                result.size == 1 &&
                result[0].title == "indicator_graph_1"
        )
    }

    @Test
    fun `Should get default analytics if settings is null and return empty list`() {
        mockEnrollmentCall()
        mockAnalyticsSettingsCall(null)
        mockRepeatableStagesCall()
        mockNumericDataElements(true)
        mockIndicators(true)
        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(
            result.isEmpty()
        )
    }

    private fun mockEnrollmentCall() {
        whenever(
            d2.enrollmentModule()
                .enrollments()
                .uid(any())
                .blockingGet()
        ) doReturn Enrollment.builder()
            .uid("enrollmentUid")
            .program("programUid")
            .trackedEntityInstance("teiUid")
            .build()
    }

    private fun mockAnalyticsSettingsCall(result: MutableList<AnalyticsTeiSetting>?) {
        whenever(
            d2.settingModule().analyticsSetting().teis()
        ) doReturn mock()
        whenever(
            d2.settingModule().analyticsSetting().teis()
                .byProgram()
        ) doReturn mock()
        whenever(
            d2.settingModule().analyticsSetting().teis()
                .byProgram().eq("programUid")
        ) doReturn mock()
        whenever(
            d2.settingModule().analyticsSetting().teis()
                .byProgram().eq("programUid")
                .blockingGet()
        ) doReturn result
    }

    private fun mockRepeatableStagesCall() {
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("programUid")
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("programUid")
                .byRepeatable()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("programUid")
                .byRepeatable().eq(true)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("programUid")
                .byRepeatable().eq(true)
                .blockingGet()
        ) doReturn listOf(
            ProgramStage.builder()
                .uid("stage_1")
                .build()
        )
    }

    private fun mockIndicators(emptyList: Boolean) {
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid()
        ) doReturn mock()
        whenever(
            d2.programModule().programIndicators()
                .byDisplayInForm().isTrue
                .byProgramUid().eq("programUid")
        ) doReturn mock()
        if (emptyList) {
            whenever(
                d2.programModule().programIndicators()
                    .byDisplayInForm().isTrue
                    .byProgramUid().eq("programUid")
                    .blockingGet()
            ) doReturn emptyList()
        } else {
            whenever(
                d2.programModule().programIndicators()
                    .byDisplayInForm().isTrue
                    .byProgramUid().eq("programUid")
                    .blockingGet()
            ) doReturn listOf(
                ProgramIndicator.builder()
                    .uid("indicator_1")
                    .build()
            )
        }
    }

    private fun mockNumericDataElements(emptyList: Boolean) {
        val de = DataElement.builder().uid("de_1")
            .valueType(ValueType.NUMBER)
            .build()
        whenever(
            d2.programModule().programStageDataElements()
                .byProgramStage().eq("stage_1")
        ) doReturn mock()
        if (emptyList) {
            whenever(
                d2.programModule().programStageDataElements()
                    .byProgramStage().eq("stage_1")
                    .blockingGet()
            ) doReturn listOf()
        } else {
            whenever(
                d2.programModule().programStageDataElements()
                    .byProgramStage().eq("stage_1")
                    .blockingGet()
            ) doReturn listOf(
                ProgramStageDataElement.builder()
                    .uid("psde_uid_1")
                    .dataElement(DataElement.builder().uid("de_1").build())
                    .build()
            )
        }

        whenever(
            d2.dataElementModule().dataElements().uid("de_1")
        ) doReturn mock()
        whenever(
            d2.dataElementModule().dataElements().uid("de_1")
                .blockingGet()
        ) doReturn de
    }

    private fun mockedAnalyticsSettings(): MutableList<AnalyticsTeiSetting>? {
        return arrayListOf(
            AnalyticsTeiSetting.builder()
                .uid("analyticsTeiSettings_1")
                .name("settings_1")
                .shortName("settings_1")
                .program("programUid")
                .period(PeriodType.Weekly)
                .type(ChartType.LINE)
                .data(AnalyticsTeiData.builder().build())
                .build()
        )
    }

    private fun mockedSettingsGraphs(): List<Graph> {
        return arrayListOf(
            Graph(
                "settings_1",
                false,
                emptyList(),
                "periodToDisplay",
                PeriodType.Daily,
                0L,
                dhis2.org.analytics.charts.data.ChartType.LINE_CHART
            )
        )
    }

    private fun mockedDataElementGraph(): Graph {
        return Graph(
            "de_graph_1",
            false,
            listOf(SerieData("de_field", listOf(GraphPoint(Date(), null, 30f)))),
            "periodToDisplay",
            PeriodType.Daily,
            0L,
            dhis2.org.analytics.charts.data.ChartType.LINE_CHART
        )
    }

    private fun mockedIndicatorGraph(): Graph {
        return Graph(
            "indicator_graph_1",
            false,
            listOf(SerieData("indicator_field", listOf(GraphPoint(Date(), null, 30f)))),
            "periodToDisplay",
            PeriodType.Daily,
            0L,
            dhis2.org.analytics.charts.data.ChartType.LINE_CHART
        )
    }
}
