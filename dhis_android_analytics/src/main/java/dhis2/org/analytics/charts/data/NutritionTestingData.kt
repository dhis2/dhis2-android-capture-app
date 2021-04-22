package dhis2.org.analytics.charts.data

import dhis2.org.analytics.charts.providers.PeriodStepProviderImpl
import dhis2.org.analytics.charts.providers.RuleEngineNutritionDataProviderImpl
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

fun List<Graph>.nutritionTestingData(d2: D2): List<Graph> {
    val nutritionDataProvider = RuleEngineNutritionDataProviderImpl()
    val monthlyPeriodStep = PeriodStepProviderImpl(d2).periodStep(PeriodType.Monthly)
    return toMutableList().apply {
        val series =
            nutritionDataProvider.getNutritionData(NutritionChartType.WHO_HFA_BOY)
                .toMutableList().apply {
                    add(
                        SerieData(
                            "HFA zScore Value",
                            listOf(
                                GraphPoint(Date(2020, 0, 1), 0f, 50f),
                                GraphPoint(Date(2020, 10, 1), 10f, 65f),
                                GraphPoint(Date(2021, 8, 1), 20f, 70f),
                                GraphPoint(Date(2022, 6, 1), 30f, 83f),
                                GraphPoint(Date(2023, 4, 1), 40f, 90f),
                                GraphPoint(Date(2024, 7, 1), 55f, 110f)
                            )
                        )
                    )
                }
        val seriesWFA =
            nutritionDataProvider.getNutritionData(NutritionChartType.WHO_WFA_BOY)
                .toMutableList().apply {
                    add(
                        SerieData(
                            "WFA zScore Value",
                            listOf(
                                GraphPoint(Date(2020, 0, 1), 0f, 3f),
                                GraphPoint(Date(2020, 10, 1), 10f, 7f),
                                GraphPoint(Date(2021, 8, 1), 20f, 9f),
                                GraphPoint(Date(2022, 6, 1), 30f, 12f),
                                GraphPoint(Date(2023, 4, 1), 40f, 14f),
                                GraphPoint(Date(2024, 7, 1), 55f, 17f)
                            )
                        )
                    )
                }
        val seriesWFH =
            nutritionDataProvider.getNutritionData(NutritionChartType.WHO_WFH_BOY)
                .toMutableList().apply {
                    add(
                        SerieData(
                            "WFH zScore Value",
                            listOf(
                                GraphPoint(Date(2020, 0, 1), 45f, 1f),
                                GraphPoint(Date(2020, 10, 1), 50f, 3f),
                                GraphPoint(Date(2021, 8, 1), 60f, 7f),
                                GraphPoint(Date(2022, 6, 1), 80f, 10f),
                                GraphPoint(Date(2023, 4, 1), 90f, 13f),
                                GraphPoint(Date(2024, 7, 1), 120f, 22f)
                            )
                        )
                    )
                }

        add(
            Graph(
                "HFA - Boy",
                false,
                series,
                "",
                PeriodType.Monthly,
                monthlyPeriodStep,
                ChartType.NUTRITION
            )
        )
        add(
            Graph(
                "WFA - Boy",
                false,
                seriesWFA,
                "",
                PeriodType.Monthly,
                monthlyPeriodStep,
                ChartType.NUTRITION
            )
        )
        add(
            Graph(
                "WFH - Boy",
                false,
                seriesWFH,
                "",
                PeriodType.Monthly,
                monthlyPeriodStep,
                ChartType.NUTRITION
            )
        )
    }
}