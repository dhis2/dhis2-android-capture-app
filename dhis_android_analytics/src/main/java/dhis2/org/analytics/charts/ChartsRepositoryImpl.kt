package dhis2.org.analytics.charts

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.nutritionTestingData
import dhis2.org.analytics.charts.data.pieChartTestingData
import dhis2.org.analytics.charts.data.radarTestingData
import dhis2.org.analytics.charts.data.visualizationGroupTestingData
import dhis2.org.analytics.charts.mappers.AnalyticsTeiSettingsToGraph
import dhis2.org.analytics.charts.mappers.DataElementToGraph
import dhis2.org.analytics.charts.mappers.ProgramIndicatorToGraph
import dhis2.org.analytics.charts.mappers.VisualizationToGraph
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.analytics.aggregated.DimensionItem
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsSetting

class ChartsRepositoryImpl(
    private val d2: D2,
    private val visualizationToGraph: VisualizationToGraph,
    private val analyticsTeiSettingsToGraph: AnalyticsTeiSettingsToGraph,
    private val dataElementToGraph: DataElementToGraph,
    private val programIndicatorToGraph: ProgramIndicatorToGraph,
    private val featureConfig: FeatureConfigRepository
) : ChartsRepository {

    override fun getAnalyticsForEnrollment(enrollmentUid: String): List<Graph> {
        val enrollment = getEnrollment(enrollmentUid)
        if (enrollment.trackedEntityInstance() == null) return emptyList()

        val settingsAnalytics = getSettingsAnalytics(enrollment)
        return if (settingsAnalytics.isNotEmpty() &&
            !featureConfig.isFeatureEnable(Feature.FORCE_DEFAULT_ANALYTICS)
        ) {
            settingsAnalytics
        } else {
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
                    else ->
                        emptyList<AnalyticsDhisVisualizationsGroup>()
                            .visualizationGroupTestingData(featureConfig)
                }
            } ?: emptyList<AnalyticsDhisVisualizationsGroup>()
            .visualizationGroupTestingData(featureConfig)
    }

    override fun getDataSetVisualization(groupUid: String?, programUid: String): List<Graph> {
        return emptyList<Graph>().nutritionTestingData(d2)
    }

    override fun getProgramVisualization(groupUid: String?, programUid: String): List<Graph> {
        val graphList = mutableListOf<Graph>()
        val visualizationSettings: AnalyticsDhisVisualizationsSetting? =
            d2.settingModule().analyticsSetting()
                .visualizationsSettings()
                .blockingGet()

        visualizationSettings
            ?.program()?.get(programUid)?.find { it.id().equals(groupUid) }
            ?.let { visualizationGroup -> addVisualizationsInGroup(visualizationGroup, graphList) }

        return when {
            graphList.isEmpty() && featureConfig.isFeatureEnable(Feature.FORCE_DEFAULT_ANALYTICS) ->
                graphList.nutritionTestingData(d2)
            else -> graphList
        }
    }

    override fun getHomeVisualization(groupUid: String?): List<Graph> {
        val graphList = mutableListOf<Graph>()
        val visualizationSettings = d2.settingModule().analyticsSetting()
            .visualizationsSettings()
            .blockingGet()

        visualizationSettings
            ?.home()?.find {
            if (groupUid != null) {
                it.id() == groupUid
            } else {
                true
            }
        }
            ?.let { visualizationGroup -> addVisualizationsInGroup(visualizationGroup, graphList) }

        return when {
            graphList.isEmpty() && featureConfig.isFeatureEnable(Feature.FORCE_DEFAULT_ANALYTICS) ->
                graphList.nutritionTestingData(d2)
            else -> graphList
        }
    }

    private fun addVisualizationsInGroup(
        visualizationGroup: AnalyticsDhisVisualizationsGroup,
        graphList: MutableList<Graph>
    ) {
        visualizationGroup.visualizations().forEach { analyticVisualization ->
            val visualizationUid = analyticVisualization.uid()
            val visualization = d2.visualizationModule()
                .visualizations()
                .uid(visualizationUid)
                .blockingGet()
            val selectedRelativePeriod = visualizationPeriod(visualizationUid)
            val selectedOrgUnits = visualizationOrgUnits(visualizationUid)

            val gridAnalyticsResponse = d2.analyticsModule()
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
                    selectedOrgUnits?.map { ouUid: String ->
                        DimensionItem.OrganisationUnitItem.Absolute(ouUid)
                    }?.let { dimensionOrgUnits ->
                        withOrganisationUnits(dimensionOrgUnits)
                    } ?: this
                }
                .blockingEvaluate()

            visualizationToGraph.mapToGraph(
                visualization,
                gridAnalyticsResponse,
                selectedRelativePeriod?.firstOrNull(),
                selectedOrgUnits
            ).let {
                graphList.add(it)
            }
        }
    }

    private fun getSettingsAnalytics(enrollment: Enrollment): List<Graph> {
        return d2.settingModule().analyticsSetting().teis()
            .byProgram().eq(enrollment.program())
            .blockingGet()?.let { analyticsSettings ->
            analyticsTeiSettingsToGraph.map(
                enrollment.trackedEntityInstance()!!,
                analyticsSettings,
                { dataElementUid ->
                    d2.dataElementModule().dataElements().uid(dataElementUid).blockingGet()
                        .displayFormName() ?: dataElementUid
                },
                { indicatorUid ->
                    d2.programModule().programIndicators().uid(indicatorUid).blockingGet()
                        .displayName() ?: indicatorUid
                },
                { nutritionGenderData ->
                    val genderValue =
                        d2.trackedEntityModule().trackedEntityAttributeValues().value(
                            nutritionGenderData.attributeUid,
                            enrollment.trackedEntityInstance()
                        ).blockingGet()
                    nutritionGenderData.isFemale(genderValue?.value())
                }
            )
        } ?: emptyList()
    }

    private fun getDefaultAnalytics(enrollment: Enrollment): List<Graph> {
        return getRepeatableProgramStages(enrollment.program()).map { programStage ->

            val period = programStage.periodType() ?: PeriodType.Daily

            getNumericDataElements(programStage.uid()).map { dataElement ->
                dataElementToGraph.map(
                    dataElement,
                    programStage.uid(),
                    enrollment.trackedEntityInstance()!!,
                    period
                )
            }.union(
                getStageIndicators(enrollment.program()).map { programIndicator ->
                    programIndicatorToGraph.map(
                        programIndicator,
                        programStage.uid(),
                        enrollment.trackedEntityInstance()!!,
                        period
                    )
                }
            )
        }.flatten()
            .filter { it.series.isNotEmpty() }
            .radarTestingData(d2, featureConfig)
            .pieChartTestingData(d2, featureConfig)
    }

    private fun getRepeatableProgramStages(program: String?) =
        d2.programModule().programStages()
            .byProgramUid().eq(program)
            .byRepeatable().eq(true)
            .blockingGet()

    private fun getEnrollment(enrollmentUid: String) =
        d2.enrollmentModule().enrollments()
            .uid(enrollmentUid)
            .blockingGet()

    private fun getNumericDataElements(stageUid: String): List<DataElement> {
        return d2.programModule().programStageDataElements()
            .byProgramStage().eq(stageUid)
            .blockingGet().filter {
                d2.dataElementModule().dataElements().uid(it.dataElement()?.uid())
                    .blockingGet().valueType()?.isNumeric ?: false
            }.map {
                d2.dataElementModule().dataElements().uid(
                    it.dataElement()?.uid()
                ).blockingGet()
            }
    }

    private fun getStageIndicators(programUid: String?): List<ProgramIndicator> {
        return d2.programModule().programIndicators()
            .byDisplayInForm().isTrue
            .byProgramUid().eq(programUid)
            .blockingGet()
    }

    override fun setVisualizationPeriods(visualizationUid: String, periods: List<RelativePeriod>) {
        if (periods.isNotEmpty()) {
            d2.dataStoreModule().localDataStore()
                .value("${visualizationUid}_p")
                .blockingSet(
                    Gson().toJson(periods)
                )
        } else {
            d2.dataStoreModule().localDataStore()
                .value("${visualizationUid}_p")
                .blockingDeleteIfExist()
        }
    }

    override fun setVisualizationOrgUnits(
        visualizationUid: String,
        orgUnits: List<OrganisationUnit>
    ) {
        if (orgUnits.isNotEmpty()) {
            d2.dataStoreModule().localDataStore()
                .value("${visualizationUid}_ou")
                .blockingSet(
                    Gson().toJson(orgUnits.map { it.uid() })
                )
        } else {
            d2.dataStoreModule().localDataStore()
                .value("${visualizationUid}_ou")
                .blockingDeleteIfExist()
        }
    }

    private fun visualizationPeriod(visualizationUid: String): List<RelativePeriod>? {
        return if (d2.dataStoreModule().localDataStore().value("${visualizationUid}_p")
            .blockingExists()
        ) {
            val entry =
                d2.dataStoreModule().localDataStore()
                    .value("${visualizationUid}_p")
                    .blockingGet()
            val type = object : TypeToken<List<RelativePeriod>?>() {}.type
            return entry.value()?.let { Gson().fromJson(entry.value(), type) }
        } else {
            null
        }
    }

    private fun visualizationOrgUnits(visualizationUid: String): List<String>? {
        return if (d2.dataStoreModule().localDataStore().value("${visualizationUid}_ou")
            .blockingExists()
        ) {
            val entry =
                d2.dataStoreModule().localDataStore()
                    .value("${visualizationUid}_ou")
                    .blockingGet()
            val type = object : TypeToken<List<String>?>() {}.type
            return entry.value()?.let { Gson().fromJson(entry.value(), type) }
        } else {
            null
        }
    }
}
