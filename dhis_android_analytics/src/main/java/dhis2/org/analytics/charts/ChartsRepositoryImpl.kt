package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import java.text.SimpleDateFormat
import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.period.PeriodType

class ChartsRepositoryImpl(private val d2: D2) : ChartsRepository {

    override fun getAnalyticsForEnrollment(enrollmentUid: String): List<Graph> {
        val enrollment = getEnrollment(enrollmentUid)

        return getRepeatableProgramStages(enrollment.program()).map { programStage ->

            getNumericDataElements(programStage.uid()).map { dataElement ->

                val coordinates = getCoordinatesSortedByDate(
                    programStage.uid(),
                    enrollment.trackedEntityInstance(),
                    dataElement.uid()
                )

                val period = programStage.periodType() ?: PeriodType.Daily

                Graph(
                    "${period.name}-${dataElement.displayFormName()}",
                    false,
                    coordinates,
                    "",
                    programStage.periodType() ?: PeriodType.Daily,
                    periodStep(programStage.periodType())
                )
            }
        }.flatten()
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
                        formattedDate(lineListResponse.date),
                        value.toFloat()
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
