package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

class ChartsRepositoryImpl(private val d2: D2) : ChartsRepository {

    override fun getAnalyticsForEnrollment(enrollmentUid: String): List<Graph> {
        val enrollment = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()
        return d2.programModule().programStages()
            .byProgramUid().eq(enrollment.program())
            .byRepeatable().isTrue
            .blockingGet()
            .filter {
                it.repeatable() == true
            }.map { programStage ->
                val numericDataElements = d2.programModule().programStageDataElements()
                    .byProgramStage().eq(programStage.uid())
                    .blockingGet().filter {
                        d2.dataElementModule().dataElements().uid(it.dataElement()?.uid())
                            .blockingGet().valueType()?.isNumeric ?: false
                    }.map {
                        d2.dataElementModule().dataElements().uid(
                            it.dataElement()?.uid()
                        ).blockingGet()
                    }
                numericDataElements.map { dataElement ->
                    val graphTitle =
                        "${programStage.displayName()}-${dataElement.displayFormName()}"
                    val isOnline = false
                    val periodToDisplay = ""
                    val eventPeriod = programStage.periodType() ?: PeriodType.Daily
                    val coordinates = d2.eventModule().events()
                        .byEnrollmentUid().eq(enrollmentUid)
                        .byProgramStageUid().eq(programStage.uid())
                        .byDeleted().isFalse
                        .orderByEventDate(RepositoryScope.OrderByDirection.ASC)
                        .byStatus().`in`(EventStatus.ACTIVE, EventStatus.COMPLETED)
                        .blockingGet()
                        .filter { event ->
                            d2.trackedEntityModule().trackedEntityDataValues()
                                .value(event.uid(), dataElement.uid()).blockingExists()
                        }.map { event ->
                            val date = event.eventDate()!!
                            val value = d2.trackedEntityModule().trackedEntityDataValues()
                                .value(event.uid(), dataElement.uid()).blockingGet().value()
                            GraphPoint(
                                date,
                                value!!.toFloat()
                            )
                        }

                    val initialPeriodDate =
                        d2.periodModule().periodHelper().blockingGetPeriodForPeriodTypeAndDate(
                            programStage.periodType() ?: PeriodType.Daily,
                            coordinates.first().eventDate,
                            -1
                        ).startDate()?.time ?: 0L
                    val periodStep = coordinates.first().eventDate.time - initialPeriodDate

                    Graph(
                        graphTitle,
                        isOnline,
                        coordinates,
                        periodToDisplay,
                        eventPeriod,
                        periodStep
                    )
                }
            }.flatten()


    }
}