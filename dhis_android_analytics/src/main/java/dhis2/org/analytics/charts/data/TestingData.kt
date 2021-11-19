package dhis2.org.analytics.charts.data

import dhis2.org.analytics.charts.providers.PeriodStepProviderImpl
import dhis2.org.analytics.charts.providers.RuleEngineNutritionDataProviderImpl
import java.util.GregorianCalendar
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup

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
                series,
                RelativePeriod.LAST_4_WEEKS,
                PeriodType.Monthly,
                monthlyPeriodStep,
                ChartType.NUTRITION
            )
        )
        add(
            Graph(
                "WFA - Boy",
                seriesWFA,
                RelativePeriod.LAST_4_WEEKS,
                PeriodType.Monthly,
                monthlyPeriodStep,
                ChartType.NUTRITION
            )
        )
        add(
            Graph(
                "WFH - Boy",
                seriesWFH,
                RelativePeriod.LAST_4_WEEKS,
                PeriodType.Monthly,
                monthlyPeriodStep,
                ChartType.NUTRITION
            )
        )
    }
}

fun List<Graph>.radarTestingData(d2: D2, featureConfig: FeatureConfigRepository): List<Graph> {
    return if (!featureConfig.isFeatureEnable(Feature.ANDROAPP_2557)) {
        return this
    } else {
        toMutableList().apply {
            val monthlyPeriodStep = PeriodStepProviderImpl(d2).periodStep(PeriodType.Daily)
            val serieA = SerieData(
                "Test A",
                listOf(
                    GraphPoint(GregorianCalendar(2020, 0, 1).time, -1f, 3f),
                    GraphPoint(GregorianCalendar(2020, 10, 1).time, -1f, 7f),
                    GraphPoint(GregorianCalendar(2021, 8, 1).time, -1f, 9f),
                    GraphPoint(GregorianCalendar(2022, 6, 1).time, -1f, 12f),
                    GraphPoint(GregorianCalendar(2023, 4, 1).time, -1f, 14f),
                    GraphPoint(GregorianCalendar(2024, 7, 1).time, -1f, 17f)
                )
            )
            val serieB = SerieData(
                "Test B",
                listOf(
                    GraphPoint(GregorianCalendar(2020, 0, 1).time, 0f, 13f),
                    GraphPoint(GregorianCalendar(2020, 10, 1).time, 10f, 9f),
                    GraphPoint(GregorianCalendar(2021, 8, 1).time, 20f, 20f),
                    GraphPoint(GregorianCalendar(2022, 6, 1).time, 30f, 1f),
                    GraphPoint(GregorianCalendar(2023, 4, 1).time, 40f, 4f),
                    GraphPoint(GregorianCalendar(2024, 7, 1).time, 55f, 8f)
                )
            )
            val radarSeries = listOf(serieA, serieB)
            add(
                Graph(
                    "Radar test",
                    radarSeries,
                    RelativePeriod.LAST_4_WEEKS,
                    PeriodType.Daily,
                    monthlyPeriodStep,
                    ChartType.RADAR,
                    listOf("HP", "ATK", "DEF", "S.ATK", "S.DEF", "SPD")
                )
            )
        }
    }
}

fun List<Graph>.pieChartTestingData(d2: D2, featureConfig: FeatureConfigRepository): List<Graph> {
    if (!featureConfig.isFeatureEnable(Feature.ANDROAPP_2557)) {
        return this
    } else {
        val dailyPeriodStep = PeriodStepProviderImpl(d2).periodStep(PeriodType.Daily)
        return toMutableList().apply {
            val caseDetectionData = listOf(
                SerieData(
                    "Case detection",
                    listOf(
                        GraphPoint(GregorianCalendar(2020, 0, 1).time, null, 14f, "PROACTIVE"),
                        GraphPoint(GregorianCalendar(2020, 10, 1).time, null, 50f, "REACTIVE"),
                        GraphPoint(GregorianCalendar(2021, 8, 1).time, null, 60f, "PASSIVE")
                    )
                )
            )
            val malariaTestData = listOf(
                SerieData(
                    "Malaria test",
                    listOf(
                        GraphPoint(GregorianCalendar(2020, 0, 1).time, null, 40f, "PCR"),
                        GraphPoint(GregorianCalendar(2020, 10, 1).time, null, 15f, "RDT"),
                        GraphPoint(GregorianCalendar(2021, 8, 1).time, null, 60f, "MICROSCOPY"),
                        GraphPoint(GregorianCalendar(2021, 8, 2).time, null, 6f, "OTHER")
                    )
                )
            )
            val genderData = listOf(
                SerieData(
                    "Gender",
                    listOf(
                        GraphPoint(GregorianCalendar(2020, 0, 1).time, null, 59f, "Female"),
                        GraphPoint(GregorianCalendar(2020, 10, 1).time, null, 41f, "Male")
                    )
                )
            )

            add(
                Graph(
                    "Daily - Case detection",
                    caseDetectionData,
                    RelativePeriod.LAST_4_WEEKS,
                    PeriodType.Daily,
                    dailyPeriodStep,
                    ChartType.PIE_CHART
                )
            )

            add(
                Graph(
                    "Daily - Malaria test",
                    malariaTestData,
                    RelativePeriod.LAST_4_WEEKS,
                    PeriodType.Daily,
                    dailyPeriodStep,
                    ChartType.PIE_CHART
                )
            )
            add(
                Graph(
                    "Daily - Gender",
                    genderData,
                    RelativePeriod.LAST_4_WEEKS,
                    PeriodType.Daily,
                    dailyPeriodStep,
                    ChartType.PIE_CHART
                )
            )
        }
    }
}

fun List<AnalyticsDhisVisualizationsGroup>.visualizationGroupTestingData(
    featureConfig: FeatureConfigRepository
): List<AnalyticsDhisVisualizationsGroup> {
    return if (!featureConfig.isFeatureEnable(Feature.ANDROAPP_2557_VG)) {
        this
    } else {
        toMutableList().apply {
            repeat(9) {
                add(
                    AnalyticsDhisVisualizationsGroup.builder()
                        .name("Group $it")
                        .id("$it")
                        .visualizations(listOf())
                        .build()
                )
            }
        }
    }
}
