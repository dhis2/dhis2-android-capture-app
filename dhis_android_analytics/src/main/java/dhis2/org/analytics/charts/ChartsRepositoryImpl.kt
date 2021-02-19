package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.NutritionChartType
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.data.SettingsAnalyticModel
import dhis2.org.analytics.charts.providers.NutritionDataProvider
import java.text.SimpleDateFormat
import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramIndicator

class ChartsRepositoryImpl(
    private val d2: D2,
    private val nutritionDataProvider: NutritionDataProvider
) : ChartsRepository {

    override fun getAnalyticsForEnrollment(enrollmentUid: String): List<Graph> {
        val enrollment = getEnrollment(enrollmentUid)

        val settingsAnalytics = getSettingsAnalytics(enrollment)
        return if (settingsAnalytics.isNotEmpty()) {
            settingsAnalytics
        } else {
            getDefaultAnalytics(enrollment)
        }
    }

//    TODO: [ANDROAPP-3644] https://jira.dhis2.org/browse/ANDROAPP-3644
    private fun getSettingsAnalytics(enrollment: Enrollment): List<Graph> {
        return listOf<SettingsAnalyticModel>().map { analyticsSetting ->

            val nutritionCoordinates: List<SerieData> =
                if (analyticsSetting.type == ChartType.NUTRITION) {
                    nutritionDataProvider.getNutritionData(NutritionChartType.WHO_HFA_BOY)
                } else {
                    emptyList()
                }

            val dataElementCoordinates = analyticsSetting.dataElements.map {
                SerieData(
                    d2.dataElementModule().dataElements().uid(it.dataElementUid).blockingGet()
                        .displayFormName() ?: it.dataElementUid,
                    getCoordinatesSortedByDate(
                        it.stageUid,
                        enrollment.trackedEntityInstance(),
                        it.dataElementUid
                    )
                )
            }
            val indicatorCoordinates = analyticsSetting.indicators.map {
                SerieData(
                    d2.programModule().programIndicators().uid(it.indicatorUid).blockingGet()
                        .displayName() ?: it.indicatorUid,
                    getIndicatorsCoordinatesSortedByDate(
                        it.stageUid,
                        enrollment.trackedEntityInstance(),
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
                PeriodType.valueOf(analyticsSetting.period),
                periodStep(PeriodType.valueOf(analyticsSetting.period)),
                analyticsSetting.type
            )
        }
    }

    private fun getDefaultAnalytics(enrollment: Enrollment): List<Graph> {
        return getRepeatableProgramStages(enrollment.program()).map { programStage ->

            val period = programStage.periodType() ?: PeriodType.Daily

            getNumericDataElements(programStage.uid()).map { dataElement ->

                val coordinates = getCoordinatesSortedByDate(
                    programStage.uid(),
                    enrollment.trackedEntityInstance(),
                    dataElement.uid()
                )

                val serie = if (coordinates.isNotEmpty()) {
                    listOf(
                        SerieData(
                            dataElement.displayFormName() ?: dataElement.uid(),
                            coordinates
                        )
                    )
                } else {
                    emptyList()
                }

                Graph(
                    "${period.name}-${dataElement.displayFormName()}",
                    false,
                    serie,
                    "",
                    programStage.periodType() ?: PeriodType.Daily,
                    periodStep(programStage.periodType())
                )
            }.union(
                getStageIndicators(enrollment.program()).map { programIndicator ->
                    val coordinates = getIndicatorsCoordinatesSortedByDate(
                        programStage.uid(),
                        enrollment.trackedEntityInstance(),
                        programIndicator.uid()
                    )
                    val serie = if (coordinates.isNotEmpty()) {
                        listOf(
                            SerieData(
                                programIndicator.displayName() ?: programIndicator.uid(),
                                coordinates
                            )
                        )
                    } else {
                        emptyList()
                    }
                    Graph(
                        "${period.name}-${programIndicator.displayName()}",
                        false,
                        serie,
                        "",
                        programStage.periodType() ?: PeriodType.Daily,
                        periodStep(programStage.periodType())
                    )
                }
            )
        }.flatten().filter { it.series.isNotEmpty() }.toMutableList().apply {
            // TODO: THIS IS JUST FOR TESTING. REMOVE ONCE IT IS APPOVED
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

            val periodStep = periodStep(PeriodType.Monthly)
            add(
                Graph(
                    "HFA - Boy",
                    false,
                    series,
                    "",
                    PeriodType.Monthly,
                    periodStep,
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
                    periodStep,
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
                    periodStep,
                    ChartType.NUTRITION
                )
            )
            // TODO: THIS IS JUST FOR TESTING. REMOVE ONCE IT IS APPOVED
        }
    }

    private fun getIndicatorsCoordinatesSortedByDate(
        programStageUid: String,
        trackedEntityInstance: String?,
        programIndicatorUid: String
    ) =
        d2.analyticsModule()
            .eventLineList()
            .byProgramStage().eq(programStageUid)
            .byTrackedEntityInstance().eq(trackedEntityInstance)
            .withProgramIndicator(programIndicatorUid)
            .blockingEvaluate()
            .sortedBy { it.date }
            .filter {
                !(it.values.first().value?.toFloat()?:Float.NaN).isNaN()
            }
            .mapNotNull { lineListResponse ->
                lineListResponse.values.first().value?.let { value ->
                    GraphPoint(
                        eventDate = formattedDate(lineListResponse.date),
                        fieldValue = value.toFloat()
                    )
                }
            }

    private fun getCoordinatesSortedByDate(
        programStageUid: String,
        trackedEntityInstance: String?,
        dataElementUid: String
    ) =
        d2.analyticsModule()
            .eventLineList()
            .byProgramStage()
            .eq(programStageUid)
            .byTrackedEntityInstance()
            .eq(trackedEntityInstance)
            .withDataElement(dataElementUid)
            .blockingEvaluate()
            .sortedBy { it.date }
            .mapNotNull { lineListResponse ->
                lineListResponse.values.first().value?.let { value ->
                    GraphPoint(
                        eventDate = formattedDate(lineListResponse.date),
                        fieldValue = value.toFloat()
                    )
                }
            }

    private fun getRepeatableProgramStages(program: String?) =
        d2.programModule()
            .programStages()
            .byProgramUid()
            .eq(program)
            .byRepeatable()
            .eq(true)
            .blockingGet()

    private fun getEnrollment(enrollmentUid: String) =
        d2.enrollmentModule()
            .enrollments()
            .uid(enrollmentUid)
            .blockingGet()

    private fun formattedDate(date: Date): Date {
        return try {
            val formattedDateString = SimpleDateFormat("yyyy-MM-dd").format(date)
            val formattedDate = SimpleDateFormat("yyyy-MM-dd").parse(formattedDateString)
            formattedDate ?: date
        } catch (e: Exception) {
            date
        }
    }

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

    private fun periodStep(periodType: PeriodType?): Long {
        val currentDate = Date()
        val initialPeriodDate =
            d2.periodModule().periodHelper().blockingGetPeriodForPeriodTypeAndDate(
                periodType ?: PeriodType.Daily,
                currentDate,
                -1
            ).startDate()?.time ?: 0L
        val currentPeriodDate =
            d2.periodModule().periodHelper().blockingGetPeriodForPeriodTypeAndDate(
                periodType ?: PeriodType.Daily,
                currentDate,
                0
            ).startDate()?.time ?: 0L
        return currentPeriodDate - initialPeriodDate
    }
}
