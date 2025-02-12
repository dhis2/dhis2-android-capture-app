package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.AnalyticResources
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphFieldValue
import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.mappers.AnalyticsTeiSettingsToGraph
import dhis2.org.analytics.charts.mappers.DataElementToGraph
import dhis2.org.analytics.charts.mappers.ProgramIndicatorToGraph
import dhis2.org.analytics.charts.mappers.VisualizationToGraph
import dhis2.org.analytics.charts.providers.AnalyticsFilterProvider
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.analytics.AnalyticsException
import org.hisp.dhis.android.core.analytics.aggregated.DimensionItem
import org.hisp.dhis.android.core.analytics.aggregated.GridAnalyticsResponse
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.RelativeOrganisationUnit
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualization
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationType
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsSetting
import org.hisp.dhis.android.core.settings.AnalyticsTeiData
import org.hisp.dhis.android.core.settings.AnalyticsTeiSetting
import org.hisp.dhis.android.core.settings.ChartType
import org.hisp.dhis.android.core.visualization.Visualization
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class ChartsRepositoryTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val visualizationToGraph: VisualizationToGraph = mock()
    private val analyticsTeiSettingsToGraph: AnalyticsTeiSettingsToGraph = mock()
    private val dataElementToGraph: DataElementToGraph = mock()
    private val programIndicatorToGraph: ProgramIndicatorToGraph = mock()
    private val analyticsResources: AnalyticResources = mock()
    private val analyticsFilterProvider: AnalyticsFilterProvider = mock()

    private val repository = ChartsRepositoryImpl(
        d2,
        visualizationToGraph,
        analyticsTeiSettingsToGraph,
        dataElementToGraph,
        programIndicatorToGraph,
        analyticsResources,
        analyticsFilterProvider,
    )

    @Test
    fun `Should return empty list if enrollment teiUid is null`() {
        whenever(
            d2.enrollmentModule()
                .enrollments()
                .uid(any())
                .blockingGet(),
        ) doReturn Enrollment.builder()
            .uid("enrollmentUid")
            .program("programUid")
            .trackedEntityInstance(null)
            .build()
        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(
            result.isEmpty(),
        )
    }

    @Test
    fun `Should get analytics if settings is not null`() {
        mockEnrollmentCall()
        mockAnalyticsSettingsCall(mockedAnalyticsSettings())
        whenever(
            analyticsTeiSettingsToGraph.map(any(), any(), any(), any(), any(), any(), any()),
        ) doReturn mockedSettingsGraphs()

        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(
            result.isNotEmpty() &&
                result.size == mockedSettingsGraphs().size &&
                result[0].title == "settings_1",
        )
    }

    @Test
    fun `Should get default analytics if settings is null`() {
        mockEnrollmentCall()
        mockAnalyticsSettingsCall(null)
        mockRepeatableStagesCall()
        mockNumericDataElements(false)

        whenever(
            dataElementToGraph.map(any(), any(), any(), any(), anyOrNull(), anyOrNull(), any()),
        ) doReturn mockedDataElementGraph()
        mockIndicators(false)
        whenever(
            programIndicatorToGraph.map(
                any(),
                any(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull(),
                any(),
            ),
        ) doReturn mockedIndicatorGraph()
        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(
            result.isNotEmpty() &&
                result.size == 2 &&
                result[0].title == "de_graph_1" &&
                result[1].title == "indicator_graph_1",
        )
    }

    @Test
    fun `Should get default analytics if settings is null and return only dataElement graphs`() {
        mockEnrollmentCall()
        mockAnalyticsSettingsCall(null)
        mockRepeatableStagesCall()
        mockNumericDataElements(false)
        whenever(
            dataElementToGraph.map(any(), any(), any(), any(), anyOrNull(), anyOrNull(), any()),
        ) doReturn mockedDataElementGraph()
        mockIndicators(true)
        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(
            result.isNotEmpty() &&
                result.size == 1 &&
                result[0].title == "de_graph_1",
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
            programIndicatorToGraph.map(
                any(),
                any(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull(),
                any(),
            ),
        ) doReturn mockedIndicatorGraph()
        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(
            result.isNotEmpty() &&
                result.size == 1 &&
                result[0].title == "indicator_graph_1",
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
            result.isEmpty(),
        )
    }

    @Test
    fun `Should return visualization groups in home`() {
        val visualizationSetting: AnalyticsDhisVisualizationsSetting = mock()
        whenever(
            d2.settingModule().analyticsSetting().visualizationsSettings().blockingGet(),
        ) doReturn visualizationSetting
        repository.getVisualizationGroups(null)
        verify(visualizationSetting).home()
    }

    @Test
    fun `Should return visualization groups in program`() {
        val mockedVisualizationGroup: AnalyticsDhisVisualizationsGroup = mock()
        val visualizationSetting: AnalyticsDhisVisualizationsSetting = mock {
            on { program() } doReturn mapOf(Pair("programUid", listOf(mockedVisualizationGroup)))
        }
        whenever(
            d2.settingModule().analyticsSetting().visualizationsSettings().blockingGet(),
        ) doReturn visualizationSetting
        val result = repository.getVisualizationGroups("programUid")
        assertTrue(result == listOf(mockedVisualizationGroup))
    }

    @Test
    fun `Should return visualization groups in data set`() {
        val mockedVisualizationGroup: AnalyticsDhisVisualizationsGroup = mock()
        val visualizationSetting: AnalyticsDhisVisualizationsSetting = mock {
            on { program() } doReturn mapOf()
            on { dataSet() } doReturn mapOf(Pair("dataSetUid", listOf(mockedVisualizationGroup)))
        }
        whenever(
            d2.settingModule().analyticsSetting().visualizationsSettings().blockingGet(),
        ) doReturn visualizationSetting
        val result = repository.getVisualizationGroups("dataSetUid")
        assertTrue(result == listOf(mockedVisualizationGroup))
    }

    @Test
    fun `Should return empty list if no visualization configured`() {
        val emptyVisualizations = AnalyticsDhisVisualizationsSetting.builder()
            .home(emptyList())
            .dataSet(emptyMap())
            .program(emptyMap())
            .build()
        whenever(
            d2.settingModule().analyticsSetting().visualizationsSettings().blockingGet(),
        ) doReturn emptyVisualizations
        val result = repository.getVisualizationGroups("dataSetUid")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Should return empty list if no visualization found`() {
        val visualizationSetting: AnalyticsDhisVisualizationsSetting = mock {
            on { program() } doReturn mapOf()
            on { dataSet() } doReturn mapOf()
        }
        whenever(
            d2.settingModule().analyticsSetting().visualizationsSettings().blockingGet(),
        ) doReturn visualizationSetting
        val result = repository.getVisualizationGroups("uid")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Should return data set visualization`() {
        mockVisualizationSettings("dataSetUid", returnDataSet = true)
        mockVisualization()

        mockedVisualizationPeriodFilterWithValue(listOf(RelativePeriod.LAST_YEAR))
        mockedVisualizationOrgUnitFilterWithValue(listOf("selectedOrgUnitUid"))
        mockedVisualizationOrgUnitFilterType(OrgUnitFilterType.SELECTION)
        mockAnalyticsResponse(RelativePeriod.LAST_YEAR, "selectedOrgUnitUid")

        repository.getDataSetVisualization("groupUid", "dataSetUid")
        verify(visualizationToGraph).mapToGraph(
            any(),
            any<Visualization>(),
            anyOrNull(),
            anyOrNull(),
        )
    }

    @Test
    fun `Should return program visualization`() {
        mockVisualizationSettings("programUid", returnProgram = true)
        mockVisualization()
        mockedVisualizationPeriodFilterWithValue()
        mockedVisualizationOrgUnitFilterWithValue()
        mockedVisualizationOrgUnitFilterType(OrgUnitFilterType.ALL)
        mockAnalyticsResponse(userOrgUnit = "selectedOrgUnit")

        repository.getProgramVisualization("groupUid", "programUid")
        verify(visualizationToGraph).mapToGraph(
            any(),
            any<Visualization>(),
            anyOrNull(),
            anyOrNull(),
        )
    }

    @Test
    fun `Should return home visualization`() {
        mockVisualizationSettings(returnHome = true)
        mockVisualization()
        mockedVisualizationPeriodFilterWithValue()
        mockedVisualizationOrgUnitFilterWithValue()
        mockedVisualizationOrgUnitFilterType(null)
        mockAnalyticsResponse()

        repository.getHomeVisualization("groupUid")
        verify(visualizationToGraph).mapToGraph(
            any(),
            any<Visualization>(),
            anyOrNull(),
            anyOrNull(),
        )
    }

    @Test
    fun `Should add error visualization`() {
        mockVisualizationSettings(returnHome = true)
        mockVisualization()
        mockedVisualizationPeriodFilterWithValue()
        mockedVisualizationOrgUnitFilterWithValue()
        mockedVisualizationOrgUnitFilterType(null)
        mockAnalyticsResponse(analyticsException = AnalyticsException.InvalidArguments("error"))

        repository.getHomeVisualization("groupUid")
        verify(analyticsResources).analyticsExceptionMessage(any())
        verify(visualizationToGraph).addErrorGraph(
            any(),
            any<Visualization>(),
            anyOrNull(),
            anyOrNull(),
        )
    }

    @Test
    fun `Should add period filter`() {
        val periods: List<RelativePeriod> = mock {
            on { isEmpty() } doReturn false
        }
        repository.setVisualizationPeriods("uid", null, periods)
        verify(analyticsFilterProvider).addPeriodFilter("uid", null, periods)
    }

    @Test
    fun `Should delete period filter`() {
        val periods: List<RelativePeriod> = mock {
            on { isEmpty() } doReturn true
        }
        repository.setVisualizationPeriods("uid", null, periods)
        verify(analyticsFilterProvider).removePeriodFilter("uid", null)
    }

    @Test
    fun `Should add org unit filter`() {
        val orgUnits: List<OrganisationUnit> = mock()
        repository.setVisualizationOrgUnits("uid1", null, emptyList(), OrgUnitFilterType.NONE)
        verify(analyticsFilterProvider).removeOrgUnitFilter("uid1", null)
        repository.setVisualizationOrgUnits("uid", null, orgUnits, OrgUnitFilterType.ALL)
        verify(analyticsFilterProvider).addOrgUnitFilter(
            "uid",
            null,
            OrgUnitFilterType.ALL,
            orgUnits,
        )
        repository.setVisualizationOrgUnits("uid", null, orgUnits, OrgUnitFilterType.SELECTION)
        verify(analyticsFilterProvider).addOrgUnitFilter(
            "uid",
            null,
            OrgUnitFilterType.SELECTION,
            orgUnits,
        )
        repository.setVisualizationOrgUnits("uid", null, emptyList(), OrgUnitFilterType.SELECTION)
        verify(analyticsFilterProvider).removeOrgUnitFilter("uid", null)
    }

    private fun mockVisualizationSettings(
        settingsUid: String? = null,
        returnProgram: Boolean = false,
        returnDataSet: Boolean = false,
        returnHome: Boolean = false,
    ) {
        val mockedAnalyticsVisualization: AnalyticsDhisVisualization = mock {
            on { name() } doReturn "name"
            on { uid() } doReturn "visualizationUid"
            on { type() } doReturn AnalyticsDhisVisualizationType.VISUALIZATION
        }
        val mockedVisualizationGroup: AnalyticsDhisVisualizationsGroup = mock {
            on { id() } doReturn "groupUid"
            on { visualizations() } doReturn listOf(mockedAnalyticsVisualization)
        }
        val mockedSetting = mapOf(Pair(settingsUid, listOf(mockedVisualizationGroup)))
        val visualizationSetting: AnalyticsDhisVisualizationsSetting = mock {
            on { program() } doReturn if (returnProgram) mockedSetting else emptyMap()
            on { dataSet() } doReturn if (returnDataSet) mockedSetting else emptyMap()
            on { home() } doReturn if (returnHome) listOf(mockedVisualizationGroup) else emptyList()
        }
        whenever(
            d2.settingModule().analyticsSetting()
                .visualizationsSettings()
                .blockingGet(),
        ) doReturn visualizationSetting
    }

    private fun mockVisualization() {
        val mockedVisualization: Visualization = mock { }
        whenever(
            d2.visualizationModule()
                .visualizations()
                .uid("visualizationUid")
                .blockingGet(),
        ) doReturn mockedVisualization
    }

    private fun mockAnalyticsResponse(
        relativePeriod: RelativePeriod? = null,
        absoluteOrgUnit: String? = null,
        userOrgUnit: String? = null,
        analyticsException: AnalyticsException? = null,
    ) {
        val mockedAnalyticResponse: GridAnalyticsResponse = mock()
        whenever(
            d2.analyticsModule().visualizations()
                .withVisualization("visualizationUid"),
        ) doReturn mock()
        whenever(
            d2.analyticsModule().visualizations()
                .withVisualization("visualizationUid")
                .run {
                    relativePeriod?.let {
                        withPeriods(listOf(DimensionItem.PeriodItem.Relative(relativePeriod)))
                    } ?: this
                },
        ) doReturn mock()
        whenever(
            d2.analyticsModule().visualizations()
                .withVisualization("visualizationUid")
                .run {
                    relativePeriod?.let {
                        withPeriods(listOf(DimensionItem.PeriodItem.Relative(relativePeriod)))
                    } ?: this
                }.run {
                    absoluteOrgUnit?.let {
                        withOrganisationUnits(
                            listOf(DimensionItem.OrganisationUnitItem.Absolute(absoluteOrgUnit)),
                        )
                    } ?: this
                }.run {
                    userOrgUnit?.let {
                        withOrganisationUnits(
                            listOf(
                                DimensionItem.OrganisationUnitItem.Relative(
                                    RelativeOrganisationUnit.USER_ORGUNIT,
                                ),
                            ),
                        )
                    } ?: this
                },
        ) doReturn mock()
        whenever(
            d2.analyticsModule().visualizations()
                .withVisualization("visualizationUid")
                .run {
                    relativePeriod?.let {
                        withPeriods(listOf(DimensionItem.PeriodItem.Relative(relativePeriod)))
                    } ?: this
                }.run {
                    absoluteOrgUnit?.let {
                        withOrganisationUnits(
                            listOf(DimensionItem.OrganisationUnitItem.Absolute(absoluteOrgUnit)),
                        )
                    } ?: this
                }
                .run {
                    userOrgUnit?.let {
                        withOrganisationUnits(
                            listOf(
                                DimensionItem.OrganisationUnitItem.Relative(
                                    RelativeOrganisationUnit.USER_ORGUNIT,
                                ),
                            ),
                        )
                    } ?: this
                }
                .blockingEvaluate(),
        ) doReturn if (analyticsException == null) {
            Result.Success(mockedAnalyticResponse)
        } else {
            Result.Failure(analyticsException)
        }
    }

    private fun mockEnrollmentCall() {
        whenever(
            d2.enrollmentModule()
                .enrollments()
                .uid(any())
                .blockingGet(),
        ) doReturn Enrollment.builder()
            .uid("enrollmentUid")
            .program("programUid")
            .trackedEntityInstance("teiUid")
            .build()
    }

    private fun mockAnalyticsSettingsCall(result: List<AnalyticsTeiSetting>?) {
        whenever(
            d2.settingModule().analyticsSetting().teis(),
        ) doReturn mock()
        whenever(
            d2.settingModule().analyticsSetting().teis()
                .byProgram(),
        ) doReturn mock()
        whenever(
            d2.settingModule().analyticsSetting().teis()
                .byProgram().eq("programUid"),
        ) doReturn mock()
        whenever(
            d2.settingModule().analyticsSetting().teis()
                .byProgram().eq("programUid")
                .blockingGet(),
        ) doReturn (result ?: emptyList())
    }

    private fun mockRepeatableStagesCall() {
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("programUid"),
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("programUid")
                .byRepeatable(),
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("programUid")
                .byRepeatable().eq(true),
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("programUid")
                .byRepeatable().eq(true)
                .blockingGet(),
        ) doReturn listOf(
            ProgramStage.builder()
                .uid("stage_1")
                .build(),
        )
    }

    private fun mockIndicators(emptyList: Boolean) {
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
        if (emptyList) {
            whenever(
                d2.programModule().programIndicators()
                    .byDisplayInForm().isTrue
                    .byProgramUid().eq("programUid")
                    .blockingGet(),
            ) doReturn emptyList()
        } else {
            whenever(
                d2.programModule().programIndicators()
                    .byDisplayInForm().isTrue
                    .byProgramUid().eq("programUid")
                    .blockingGet(),
            ) doReturn listOf(
                ProgramIndicator.builder()
                    .uid("indicator_1")
                    .build(),
            )
        }
    }

    private fun mockNumericDataElements(emptyList: Boolean) {
        val de = DataElement.builder().uid("de_1")
            .valueType(ValueType.NUMBER)
            .build()
        whenever(
            d2.programModule().programStageDataElements()
                .byProgramStage().eq("stage_1"),
        ) doReturn mock()
        if (emptyList) {
            whenever(
                d2.programModule().programStageDataElements()
                    .byProgramStage().eq("stage_1")
                    .blockingGet(),
            ) doReturn listOf()
        } else {
            whenever(
                d2.programModule().programStageDataElements()
                    .byProgramStage().eq("stage_1")
                    .blockingGet(),
            ) doReturn listOf(
                ProgramStageDataElement.builder()
                    .uid("psde_uid_1")
                    .dataElement(DataElement.builder().uid("de_1").build())
                    .build(),
            )
        }

        whenever(
            d2.dataElementModule().dataElements().uid("de_1"),
        ) doReturn mock()
        whenever(
            d2.dataElementModule().dataElements().uid("de_1")
                .blockingGet(),
        ) doReturn de
    }

    private fun mockedAnalyticsSettings(): List<AnalyticsTeiSetting> {
        return arrayListOf(
            AnalyticsTeiSetting.builder()
                .uid("analyticsTeiSettings_1")
                .name("settings_1")
                .shortName("settings_1")
                .program("programUid")
                .period(PeriodType.Weekly)
                .type(ChartType.LINE)
                .data(AnalyticsTeiData.builder().build())
                .build(),
        )
    }

    private fun mockedSettingsGraphs(): List<Graph> {
        return arrayListOf(
            Graph(
                "settings_1",
                emptyList(),
                null,
                PeriodType.Daily,
                0L,
                dhis2.org.analytics.charts.data.ChartType.LINE_CHART,
            ),
        )
    }

    private fun mockedDataElementGraph(): Graph {
        return Graph(
            title = "de_graph_1",
            series = listOf(
                SerieData(
                    "de_field",
                    listOf(GraphPoint(Date(), null, GraphFieldValue.Numeric(30f))),
                ),
            ),
            periodToDisplayDefault = null,
            eventPeriodType = PeriodType.Daily,
            periodStep = 0L,
            chartType = dhis2.org.analytics.charts.data.ChartType.LINE_CHART,
        )
    }

    private fun mockedIndicatorGraph(): Graph {
        return Graph(
            "indicator_graph_1",
            listOf(
                SerieData(
                    "indicator_field",
                    listOf(GraphPoint(Date(), null, GraphFieldValue.Numeric(30f))),
                ),
            ),
            null,
            PeriodType.Daily,
            0L,
            dhis2.org.analytics.charts.data.ChartType.LINE_CHART,
        )
    }

    private fun mockedVisualizationPeriodFilter() {
        whenever(
            d2.dataStoreModule().localDataStore().value(any()).blockingExists(),
        ) doReturn false
    }

    private fun mockedVisualizationPeriodFilterWithValue(
        relativePeriods: List<RelativePeriod>? = null,
    ) {
        whenever(
            analyticsFilterProvider.visualizationPeriod(any()),
        ) doReturn relativePeriods
    }

    private fun mockedVisualizationOrgUnitFilterType(orgUnitFilterType: OrgUnitFilterType?) {
        whenever(
            analyticsFilterProvider.visualizationOrgUnitsType(any()),
        ) doReturn orgUnitFilterType
    }

    private fun mockedVisualizationOrgUnitFilterWithValue(orgUnits: List<String>? = null) {
        whenever(
            analyticsFilterProvider.visualizationOrgUnits(any()),
        ) doReturn orgUnits
    }
}
