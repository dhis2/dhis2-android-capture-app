package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphFilters
import dhis2.org.analytics.charts.data.NutritionGenderData
import dhis2.org.analytics.charts.data.NutritionSettingsAnalyticsModel
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.data.toNutritionChartType
import dhis2.org.analytics.charts.providers.ChartCoordinatesProvider
import dhis2.org.analytics.charts.providers.NutritionDataProvider
import dhis2.org.analytics.charts.providers.PeriodStepProvider
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.settings.AnalyticsTeiSetting

class AnalyticsTeiSettingsToGraph(
    private val analyticsSettingsMapper: AnalyticTeiSettingsToSettingsAnalyticsModel,
    private val nutritionDataProvider: NutritionDataProvider,
    private val periodStepProvider: PeriodStepProvider,
    private val chartCoordinatesProvider: ChartCoordinatesProvider,
) {

    fun map(
        teiUid: String,
        analytycsTeiSettings: List<AnalyticsTeiSetting>,
        selectedRelativePeriodProvider: (String) -> List<RelativePeriod>?,
        selectedOrgUnitProvider: (String) -> List<String>?,
        dataElementNameProvider: (String) -> String,
        indicatorNameProvider: (String) -> String,
        teiGenderProvider: (NutritionGenderData) -> Boolean,
    ): List<Graph> {
        return analytycsTeiSettings.map { analyticsTeiSettings ->
            val analyticsSetting = analyticsSettingsMapper.map(analyticsTeiSettings)
            val selectedRelativePeriod = selectedRelativePeriodProvider(analyticsTeiSettings.uid())
            val selectedOrgUnits = selectedOrgUnitProvider(analyticsTeiSettings.uid())
            val nutritionCoordinates: List<SerieData> =
                if (analyticsSetting is NutritionSettingsAnalyticsModel) {
                    val isFemale = teiGenderProvider(analyticsSetting.genderData)
                    val nutritionChartType = analyticsTeiSettings.whoNutritionData()!!.chartType()
                        .toNutritionChartType(isFemale)
                    nutritionDataProvider.getNutritionData(nutritionChartType)
                        .toMutableList().apply {
                            add(
                                SerieData(
                                    analyticsSetting.displayName,
                                    chartCoordinatesProvider.nutritionCoordinates(
                                        analyticsSetting.stageUid,
                                        teiUid,
                                        analyticsSetting.zScoreContainerUid,
                                        analyticsSetting.zScoreContainerIsDataElement,
                                        analyticsSetting.ageOrHeightContainerUid,
                                        analyticsSetting.ageOrHeightIsDataElement,
                                        selectedRelativePeriod,
                                        selectedOrgUnits,
                                    ),
                                ),
                            )
                        }
                } else {
                    emptyList()
                }

            val dataElementCoordinates = analyticsSetting.dataElements().map {
                SerieData(
                    dataElementNameProvider(it.dataElementUid),
                    when (analyticsSetting.type) {
                        ChartType.PIE_CHART -> chartCoordinatesProvider.pieChartCoordinates(
                            it.stageUid,
                            teiUid,
                            it.dataElementUid,
                            selectedRelativePeriod,
                            selectedOrgUnits,
                        )
                        else -> chartCoordinatesProvider.dataElementCoordinates(
                            it.stageUid,
                            teiUid,
                            it.dataElementUid,
                            selectedRelativePeriod,
                            selectedOrgUnits,
                            true,
                        )
                    },
                )
            }
            val indicatorCoordinates = analyticsSetting.indicators().map {
                SerieData(
                    indicatorNameProvider(it.indicatorUid),
                    chartCoordinatesProvider.indicatorCoordinates(
                        it.stageUid,
                        teiUid,
                        it.indicatorUid,
                        selectedRelativePeriod,
                        selectedOrgUnits,
                    ),
                )
            }.filter { it.coordinates.isNotEmpty() }
            Graph(
                title = analyticsSetting.displayName,
                series = nutritionCoordinates.union(dataElementCoordinates)
                    .union(indicatorCoordinates)
                    .toList(),
                periodToDisplayDefault = null,
                eventPeriodType = PeriodType.valueOf(analyticsSetting.period()),
                periodStep = periodStepProvider.periodStep(
                    PeriodType.valueOf(analyticsSetting.period()),
                ),
                chartType = analyticsSetting.type,
                visualizationUid = analyticsTeiSettings.uid(),
                graphFilters = GraphFilters.Visualization(
                    periodToDisplaySelected = selectedRelativePeriod?.firstOrNull(),
                    orgUnitsSelected = selectedOrgUnits ?: emptyList(),
                ),
            )
        }
    }
}
