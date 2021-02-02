package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.rules.functions.ZScoreTable
import org.hisp.dhis.rules.functions.ZScoreTableKey
import java.text.SimpleDateFormat
import java.util.Date
import org.hisp.dhis.android.core.program.ProgramIndicator

class ChartsRepositoryImpl(private val d2: D2) : ChartsRepository {

    override fun getAnalyticsForEnrollment(enrollmentUid: String): List<Graph> {
        val enrollment = getEnrollment(enrollmentUid)

        return getRepeatableProgramStages(enrollment.program()).map { programStage ->

            val period = programStage.periodType() ?: PeriodType.Daily

            getNumericDataElements(programStage.uid()).map { dataElement ->

                val coordinates = getCoordinatesSortedByDate(
                    programStage.uid(),
                    enrollment.trackedEntityInstance(),
                    dataElement.uid()
                )

                Graph(
                    "${period.name}-${dataElement.displayFormName()}",
                    false,
                    dataElement.displayFormName() ?: dataElement.uid(),
                    listOf(coordinates),
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

                    Graph(
                        "${period.name}-${programIndicator.displayName()}",
                        false,
                        programIndicator.displayName() ?: programIndicator.uid(),
                        coordinates,
                        "",
                        programStage.periodType() ?: PeriodType.Daily,
                        periodStep(programStage.periodType())
                    )
                }
            )
        }.flatten().toMutableList().apply {
            add(
                Graph(
                    "WFA - Boy",
                    false,
                    getNutritionData(ZScoreTable.getZscoreWFATableBoy()),
                    "",
                    PeriodType.Yearly,
                    periodStep(PeriodType.Yearly),
                    ChartType.NUTRITION
                )
            )
            add(
                Graph(
                    "HFA - Boy",
                    false,
                    getNutritionData(ZScoreTable.getZscoreHFATableBoy()),
                    "",
                    PeriodType.Yearly,
                    periodStep(PeriodType.Yearly),
                    ChartType.NUTRITION
                )
            )
            add(
                Graph(
                    "WFH - Boy",
                    false,
                    getNutritionData(ZScoreTable.getZscoreWFHTableBoy()),
                    "",
                    PeriodType.Yearly,
                    periodStep(PeriodType.Yearly),
                    ChartType.NUTRITION
                )
            )
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
            .mapNotNull { lineListResponse ->
                lineListResponse.values.first().value?.let { value ->
                    GraphPoint(
                        formattedDate(lineListResponse.date),
                        value.toFloat()
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

    private fun getNutritionData(zscoreWFATableBoy: MutableMap<ZScoreTableKey, MutableMap<Float, Int>>): List<List<GraphPoint>> {
        val numberOfData = zscoreWFATableBoy.values.first().size
        val nutritionData = mutableListOf<MutableList<GraphPoint>>().apply {
            for (i in 0 until numberOfData) {
                add(mutableListOf())
            }
        }

        zscoreWFATableBoy.toSortedMap(compareBy { it.parameter })
            .values.forEachIndexed { i, map ->
                val values = map.keys.sorted()
                for(dataIndex in 0 until numberOfData){
                    nutritionData[dataIndex].add(
                        GraphPoint(
                            eventDate = Date(),
                            position = i,
                            fieldValue = values[dataIndex]
                        )
                    )
                }
            }

        return nutritionData
    }
}
