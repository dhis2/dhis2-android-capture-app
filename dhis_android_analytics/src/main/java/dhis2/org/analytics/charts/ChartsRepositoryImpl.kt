package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.bindings.hasGroup
import dhis2.org.analytics.charts.bindings.withDateFilters
import dhis2.org.analytics.charts.bindings.withFilters
import dhis2.org.analytics.charts.bindings.withOUFilters
import dhis2.org.analytics.charts.data.AnalyticResources
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphFilters
import dhis2.org.analytics.charts.mappers.AnalyticsTeiSettingsToGraph
import dhis2.org.analytics.charts.mappers.DataElementToGraph
import dhis2.org.analytics.charts.mappers.ProgramIndicatorToGraph
import dhis2.org.analytics.charts.mappers.VisualizationToGraph
import dhis2.org.analytics.charts.providers.AnalyticsFilterProvider
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.analytics.aggregated.DimensionItem
import org.hisp.dhis.android.core.analytics.trackerlinelist.TrackerLineListItem
import org.hisp.dhis.android.core.analytics.trackerlinelist.TrackerLineListResponse
import org.hisp.dhis.android.core.common.RelativeOrganisationUnit
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationType
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsSetting

class ChartsRepositoryImpl(
    private val d2: D2,
    private val visualizationToGraph: VisualizationToGraph,
    private val analyticsTeiSettingsToGraph: AnalyticsTeiSettingsToGraph,
    private val dataElementToGraph: DataElementToGraph,
    private val programIndicatorToGraph: ProgramIndicatorToGraph,
    private val analyticsResources: AnalyticResources,
    private val analyticsFilterProvider: AnalyticsFilterProvider,
) : ChartsRepository {

    private val lineListHeaderCache: MutableMap<String, List<TrackerLineListItem>> = mutableMapOf()

    override fun getAnalyticsForEnrollment(enrollmentUid: String): List<Graph> {
        val enrollment = getEnrollment(enrollmentUid)
        if (enrollment?.trackedEntityInstance() == null) return emptyList()

        val settingsAnalytics = getSettingsAnalytics(enrollment)
        return settingsAnalytics.ifEmpty {
            getDefaultAnalytics(enrollment)
        }
    }

    override fun getVisualizationGroups(uid: String?): List<AnalyticsDhisVisualizationsGroup> {
        return d2.settingModule().analyticsSetting().visualizationsSettings().blockingGet()
            ?.let { visualizationsSetting ->
                when {
                    uid == null -> {
                        visualizationsSetting.home()
                    }

                    visualizationsSetting.program().containsKey(uid) -> {
                        visualizationsSetting.program()[uid]
                    }

                    visualizationsSetting.dataSet().containsKey(uid) -> {
                        visualizationsSetting.dataSet()[uid]
                    }

                    else -> emptyList()
                }
            } ?: emptyList()
    }

    override fun getDataSetVisualization(groupUid: String?, dataSetUid: String): List<Graph> {
        val graphList = mutableListOf<Graph>()
        val visualizationSettings: AnalyticsDhisVisualizationsSetting? =
            d2.settingModule().analyticsSetting()
                .visualizationsSettings()
                .blockingGet()

        visualizationSettings
            ?.dataSet()?.get(dataSetUid)?.filter { visualizationGroup ->
                visualizationGroup.hasGroup(groupUid)
            }?.forEach { visualizationGroup ->
                addVisualizationsInGroup(visualizationGroup, graphList)
            }

        return graphList
    }

    override fun getProgramVisualization(groupUid: String?, programUid: String): List<Graph> {
        val graphList = mutableListOf<Graph>()
        val visualizationSettings: AnalyticsDhisVisualizationsSetting? =
            d2.settingModule().analyticsSetting()
                .visualizationsSettings()
                .blockingGet()

        visualizationSettings
            ?.program()?.get(programUid)?.filter {
                if (groupUid != null) {
                    it.id() == groupUid
                } else {
                    true
                }
            }
            ?.forEach { visualizationGroup ->
                addVisualizationsInGroup(
                    visualizationGroup,
                    graphList,
                )
            }

        return graphList
    }

    override fun getHomeVisualization(groupUid: String?): List<Graph> {
        val graphList = mutableListOf<Graph>()
        val visualizationSettings = d2.settingModule().analyticsSetting()
            .visualizationsSettings()
            .blockingGet()

        visualizationSettings
            ?.home()?.filter {
                if (groupUid != null) {
                    it.id() == groupUid
                } else {
                    true
                }
            }
            ?.forEach { visualizationGroup ->
                addVisualizationsInGroup(
                    visualizationGroup,
                    graphList,
                )
            }

        return graphList
    }

    private fun addVisualizationsInGroup(
        visualizationGroup: AnalyticsDhisVisualizationsGroup,
        graphList: MutableList<Graph>,
    ) {
        visualizationGroup.visualizations().forEach { analyticVisualization ->
            val customTitle = analyticVisualization.takeIf {
                it.name()?.isNotEmpty() == true
            }?.name()
            val analyticsUid = analyticVisualization.uid()

            when (analyticVisualization.type()) {
                AnalyticsDhisVisualizationType.VISUALIZATION -> addVisualization(
                    analyticsUid,
                    customTitle,
                    graphList,
                )

                AnalyticsDhisVisualizationType.TRACKER_VISUALIZATION -> addLineListing(
                    analyticsUid,
                    customTitle,
                    graphList,
                )
            }
        }
    }

    private fun addVisualization(
        visualizationUid: String,
        customTitle: String?,
        graphList: MutableList<Graph>,
    ) {
        val selectedRelativePeriod =
            analyticsFilterProvider.visualizationPeriod(visualizationUid)
        val selectedOrgUnits = analyticsFilterProvider.visualizationOrgUnits(visualizationUid)
        val selectedOrgUnitType =
            analyticsFilterProvider.visualizationOrgUnitsType(visualizationUid)

        val graphFilters = GraphFilters.Visualization(
            orgUnitsDefault = emptyList(),
            orgUnitsSelected = selectedOrgUnits ?: emptyList(),
            periodToDisplaySelected = selectedRelativePeriod?.firstOrNull(),
        )

        val visualization = d2.visualizationModule()
            .visualizations()
            .uid(visualizationUid)
            .blockingGet()

        d2.analyticsModule()
            .visualizations()
            .withVisualization(visualizationUid)
            .run {
                selectedRelativePeriod?.map { relPeriod: RelativePeriod ->
                    DimensionItem.PeriodItem.Relative(relPeriod)
                }?.let { dimensionPeriods ->
                    withPeriods(dimensionPeriods)
                } ?: this
            }
            .run {
                when (selectedOrgUnitType) {
                    OrgUnitFilterType.ALL -> {
                        withOrganisationUnits(
                            listOf(
                                DimensionItem.OrganisationUnitItem.Relative(
                                    RelativeOrganisationUnit.USER_ORGUNIT,
                                ),
                            ),
                        )
                    }

                    OrgUnitFilterType.SELECTION -> {
                        selectedOrgUnits?.map { ouUid: String ->
                            DimensionItem.OrganisationUnitItem.Absolute(ouUid)
                        }?.let { dimensionOrgUnits ->
                            withOrganisationUnits(dimensionOrgUnits)
                        } ?: this
                    }

                    else -> this
                }
            }
            .blockingEvaluate()
            .fold(
                { gridAnalyticsResponse ->
                    visualization?.let {
                        graphList.add(
                            visualizationToGraph.mapToGraph(
                                customTitle ?: visualization.displayFormName(),
                                visualization,
                                gridAnalyticsResponse,
                                graphFilters,
                            ),
                        )
                    }
                },
                { analyticException ->
                    analyticException.printStackTrace()
                    visualization?.let {
                        graphList.add(
                            visualizationToGraph.addErrorGraph(
                                customTitle ?: visualization.displayFormName(),
                                visualization,
                                graphFilters,
                                analyticsResources.analyticsExceptionMessage(analyticException),
                            ),
                        )
                    }
                },
            )
    }

    private fun addLineListing(
        trackerVisualizationUid: String,
        customTitle: String?,
        graphList: MutableList<Graph>,
    ) {
        val filters = analyticsFilterProvider.trackerVisualizationFilters(trackerVisualizationUid)
            ?: emptyMap()

        val periodFilters =
            analyticsFilterProvider.trackerVisualizationPeriodFilters(trackerVisualizationUid)
                ?: emptyMap()

        val selectedOrgUnitType =
            analyticsFilterProvider.trackerVisualizationOrgUnitTypeFilters(trackerVisualizationUid)
                ?: emptyMap()
        val selectedOrgUnits: Map<Int, List<String>> =
            analyticsFilterProvider.trackerVisualizationOrgUnitFilters(trackerVisualizationUid)
                ?: emptyMap()

        val graphFilters = GraphFilters.LineListing(
            lineListFilters = filters,
            orgUnitsSelected = selectedOrgUnits,
            periodToDisplaySelected = periodFilters,
        )

        val trackerVisualization = d2.visualizationModule().trackerVisualizations()
            .uid(trackerVisualizationUid)
            .blockingGet()

        d2.analyticsModule().trackerLineList()
            .withTrackerVisualization(trackerVisualizationUid)
            .run {
                var filteredRepository = this
                periodFilters.forEach { (columnIndex, periods) ->
                    lineListHeaderCache[trackerVisualizationUid]?.get(columnIndex)?.let {
                        filteredRepository = filteredRepository.withColumn(
                            column = it.withDateFilters(periods),
                        )
                    }
                }
                filteredRepository
            }
            .run {
                var filteredRepository = this
                selectedOrgUnitType.forEach { (columnIndex, orgUnitFilterType) ->

                    lineListHeaderCache[trackerVisualizationUid]?.get(columnIndex)?.let {
                        filteredRepository = filteredRepository.withColumn(
                            column = it.withOUFilters(
                                orgUnitFilterType,
                                selectedOrgUnits[columnIndex] ?: emptyList(),
                            ),
                        )
                    }
                }
                filteredRepository
            }
            .run {
                var filteredRepository = this
                if (filters.isNotEmpty()) {
                    filters.forEach { (columnIndex, value) ->
                        lineListHeaderCache[trackerVisualizationUid]?.get(columnIndex)?.let {
                            filteredRepository = filteredRepository.withColumn(
                                column = it.withFilters(value),
                            )
                        }
                    }
                }
                filteredRepository
            }
            .blockingEvaluate()
            .fold(
                { trackerLineListResponse ->
                    setLineListHeaderCache(trackerVisualizationUid, trackerLineListResponse)
                    graphList.add(
                        visualizationToGraph.mapToGraph(
                            customTitle ?: trackerVisualization?.displayName(),
                            trackerVisualization,
                            trackerLineListResponse,
                            graphFilters,
                        ),
                    )
                },
            ) { analyticException ->
                analyticException.printStackTrace()
                trackerVisualization?.let {
                    graphList.add(
                        visualizationToGraph.addErrorGraph(
                            customTitle,
                            trackerVisualization,
                            analyticsResources.analyticsExceptionMessage(analyticException),
                            graphFilters,
                        ),
                    )
                }
            }
    }

    private fun setLineListHeaderCache(
        visualisationUid: String,
        trackerLineListResponse: TrackerLineListResponse,
    ) {
        lineListHeaderCache[visualisationUid] = trackerLineListResponse.headers
    }

    private fun getSettingsAnalytics(enrollment: Enrollment): List<Graph> {
        return d2.settingModule().analyticsSetting().teis()
            .byProgram().eq(enrollment.program())
            .blockingGet().let { analyticsSettings ->
                analyticsTeiSettingsToGraph.map(
                    enrollment.trackedEntityInstance()!!,
                    analyticsSettings,
                    analyticsFilterProvider::visualizationPeriod,
                    analyticsFilterProvider::visualizationOrgUnits,
                    { dataElementUid ->
                        d2.dataElementModule().dataElements().uid(dataElementUid).blockingGet()
                            ?.displayFormName() ?: dataElementUid
                    },
                    { indicatorUid ->
                        d2.programModule().programIndicators().uid(indicatorUid).blockingGet()
                            ?.displayName() ?: indicatorUid
                    },
                    { nutritionGenderData ->
                        val genderValue =
                            d2.trackedEntityModule().trackedEntityAttributeValues().value(
                                nutritionGenderData.attributeUid,
                                enrollment.trackedEntityInstance()!!,
                            ).blockingGet()
                        nutritionGenderData.isFemale(genderValue?.value())
                    },
                )
            } ?: emptyList()
    }

    private fun getDefaultAnalytics(enrollment: Enrollment): List<Graph> {
        return getRepeatableProgramStages(enrollment.program()).map { programStage ->

            val period = programStage.periodType() ?: PeriodType.Daily

            getNumericDataElements(programStage.uid()).map { dataElement ->
                val selectedRelativePeriod =
                    analyticsFilterProvider.visualizationPeriod(
                        enrollment.trackedEntityInstance()!! +
                            programStage.uid() +
                            dataElement.uid(),
                    )
                val selectedOrgUnits =
                    analyticsFilterProvider.visualizationOrgUnits(
                        enrollment.trackedEntityInstance()!! +
                            programStage.uid() +
                            dataElement.uid(),
                    )
                dataElementToGraph.map(
                    dataElement,
                    programStage.uid(),
                    enrollment.trackedEntityInstance()!!,
                    period,
                    selectedRelativePeriod,
                    selectedOrgUnits,
                    true,
                )
            }.union(
                getStageIndicators(enrollment.program()).map { programIndicator ->
                    val selectedRelativePeriod =
                        analyticsFilterProvider.visualizationPeriod(
                            enrollment.trackedEntityInstance()!! +
                                programStage.uid() +
                                programIndicator.uid(),
                        )
                    val selectedOrgUnits =
                        analyticsFilterProvider.visualizationOrgUnits(
                            enrollment.trackedEntityInstance()!! +
                                programStage.uid() +
                                programIndicator.uid(),
                        )
                    programIndicatorToGraph.map(
                        programIndicator,
                        programStage.uid(),
                        enrollment.trackedEntityInstance()!!,
                        period,
                        selectedRelativePeriod,
                        selectedOrgUnits,
                        true,
                    )
                },
            )
        }.flatten()
            .filter { it.canBeShown() }
    }

    private fun getRepeatableProgramStages(program: String?) = d2.programModule().programStages()
        .byProgramUid().eq(program)
        .byRepeatable().eq(true)
        .blockingGet()

    private fun getEnrollment(enrollmentUid: String) = d2.enrollmentModule().enrollments()
        .uid(enrollmentUid)
        .blockingGet()

    private fun getNumericDataElements(stageUid: String): List<DataElement> {
        return d2.programModule().programStageDataElements()
            .byProgramStage().eq(stageUid)
            .blockingGet().filter {
                d2.dataElementModule().dataElements().uid(it.dataElement()?.uid())
                    .blockingGet()?.valueType()?.isNumeric ?: false
            }.mapNotNull {
                d2.dataElementModule().dataElements().uid(
                    it.dataElement()?.uid(),
                ).blockingGet()
            }
    }

    private fun getStageIndicators(programUid: String?): List<ProgramIndicator> {
        return d2.programModule().programIndicators()
            .byDisplayInForm().isTrue
            .byProgramUid().eq(programUid)
            .blockingGet()
    }

    override fun setVisualizationPeriods(
        visualizationUid: String,
        lineListingColumnId: Int?,
        periods: List<RelativePeriod>,
    ) {
        if (periods.isNotEmpty()) {
            analyticsFilterProvider.addPeriodFilter(
                visualizationUid,
                lineListingColumnId,
                periods,
            )
        } else {
            analyticsFilterProvider.removePeriodFilter(visualizationUid, lineListingColumnId)
        }
    }

    override fun setVisualizationOrgUnits(
        visualizationUid: String,
        lineListingColumnId: Int?,
        orgUnits: List<OrganisationUnit>,
        orgUnitFilterType: OrgUnitFilterType,
    ) {
        when (orgUnitFilterType) {
            OrgUnitFilterType.NONE -> analyticsFilterProvider.removeOrgUnitFilter(
                visualizationUid,
                lineListingColumnId,
            )

            OrgUnitFilterType.ALL -> analyticsFilterProvider.addOrgUnitFilter(
                visualizationUid,
                lineListingColumnId,
                orgUnitFilterType,
                orgUnits,
            )

            OrgUnitFilterType.SELECTION -> {
                if (orgUnits.isNotEmpty()) {
                    analyticsFilterProvider.addOrgUnitFilter(
                        visualizationUid,
                        lineListingColumnId,
                        orgUnitFilterType,
                        orgUnits,
                    )
                } else {
                    analyticsFilterProvider.removeOrgUnitFilter(
                        visualizationUid,
                        lineListingColumnId,
                    )
                }
            }
        }
    }

    override fun setLineListingFilter(
        trackerVisualizationUid: String,
        columnIndex: Int,
        filterValue: String?,
    ) {
        if (filterValue.isNullOrEmpty().not()) {
            analyticsFilterProvider.addColumnFilter(
                trackerVisualizationUid,
                columnIndex,
                filterValue!!,
            )
        } else {
            analyticsFilterProvider.removeColumnFilter(trackerVisualizationUid, columnIndex)
        }
    }
}
