package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.NutritionGenderData
import dhis2.org.analytics.charts.data.NutritionSettingsAnalyticsModel
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.data.toNutritionChartType
import dhis2.org.analytics.charts.providers.ChartCoordinatesProvider
import dhis2.org.analytics.charts.providers.NutritionDataProvider
import dhis2.org.analytics.charts.providers.PeriodStepProvider
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.settings.AnalyticsTeiSetting

class AnalyticsTeiSettingsToGraph(
    private val analyticsSettingsMapper: AnalyticTeiSettingsToSettingsAnalyticsModel,
    private val nutritionDataProvider: NutritionDataProvider,
    private val periodStepProvider: PeriodStepProvider,
    private val chartCoordinatesProvider: ChartCoordinatesProvider
) {

    fun map(
        teiUid: String,
        analytycsTeiSettings: List<AnalyticsTeiSetting>,
        dataElementNameProvider: (String) -> String,
        indicatorNameProvider: (String) -> String,
        teiGenderProvider: (NutritionGenderData) -> Boolean
    ): List<Graph> {
        return analytycsTeiSettings.map { analyticsTeiSettings ->
            val analyticsSetting = analyticsSettingsMapper.map(analyticsTeiSettings)
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
                                        analyticsSetting.ageOrHeightIsDataElement
                                    )
                                )
                            )
                        }
                } else {
                    emptyList()
                }

            val dataElementCoordinates = analyticsSetting.dataElements().map {
                SerieData(
                    dataElementNameProvider(it.dataElementUid),
                    chartCoordinatesProvider.dataElementCoordinates(
                        it.stageUid,
                        teiUid,
                        it.dataElementUid
                    )
                )
            }
            val indicatorCoordinates = analyticsSetting.indicators().map {
                SerieData(
                    indicatorNameProvider(it.indicatorUid),
                    chartCoordinatesProvider.indicatorCoordinates(
                        it.stageUid,
                        teiUid,
                        it.indicatorUid
                    )
                )
            }.filter { it.coordinates.isNotEmpty() }
            Graph(
                analyticsSetting.displayName,
                false,
                nutritionCoordinates.union(dataElementCoordinates).union(indicatorCoordinates)
                    .toList(),
                "",
                PeriodType.valueOf(analyticsSetting.period()),
                periodStepProvider.periodStep(PeriodType.valueOf(analyticsSetting.period())),
                analyticsSetting.type
            )
        }
    }
}
