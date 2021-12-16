package dhis2.org.analytics.charts.data

import dhis2.org.analytics.charts.providers.PeriodStepProviderImpl
import dhis2.org.analytics.charts.providers.RuleEngineNutritionDataProviderImpl
import java.util.GregorianCalendar
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType

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
                                GraphPoint(GregorianCalendar(2020, 0, 1).time, 0f, 50f),
                                GraphPoint(GregorianCalendar(2020, 10, 1).time, 10f, 65f),
                                GraphPoint(GregorianCalendar(2021, 8, 1).time, 20f, 70f),
                                GraphPoint(GregorianCalendar(2022, 6, 1).time, 30f, 83f),
                                GraphPoint(GregorianCalendar(2023, 4, 1).time, 40f, 90f),
                                GraphPoint(GregorianCalendar(2024, 7, 1).time, 55f, 110f)
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
                                GraphPoint(GregorianCalendar(2020, 0, 1).time, 0f, 3f),
                                GraphPoint(GregorianCalendar(2020, 10, 1).time, 10f, 7f),
                                GraphPoint(GregorianCalendar(2021, 8, 1).time, 20f, 9f),
                                GraphPoint(GregorianCalendar(2022, 6, 1).time, 30f, 12f),
                                GraphPoint(GregorianCalendar(2023, 4, 1).time, 40f, 14f),
                                GraphPoint(GregorianCalendar(2024, 7, 1).time, 55f, 17f)
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
                                GraphPoint(GregorianCalendar(2020, 0, 1).time, 45f, 1f),
                                GraphPoint(GregorianCalendar(2020, 10, 1).time, 50f, 3f),
                                GraphPoint(GregorianCalendar(2021, 8, 1).time, 60f, 7f),
                                GraphPoint(GregorianCalendar(2022, 6, 1).time, 80f, 10f),
                                GraphPoint(GregorianCalendar(2023, 4, 1).time, 90f, 13f),
                                GraphPoint(GregorianCalendar(2024, 7, 1).time, 120f, 22f)
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
