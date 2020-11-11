package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType

class ChartsRepositoryImpl(
    private val d2: D2,
    private val baseRepository: BaseRepository
) : ChartsRepository {

    override fun getAnalyticsForEnrollment(enrollmentUid: String): List<Graph> {
        return baseRepository.repeatableStagesForEnrollment(enrollmentUid)
            .map { programStage ->
                baseRepository.numericDataElements(programStage.uid()).map { dataElement ->
                    val coordinates = baseRepository.activeAndCompletedEventsWithData(
                        enrollmentUid,
                        programStage.uid(),
                        dataElement.uid()
                    ).map { eventAndValue ->
                        GraphPoint(
                            eventAndValue.first.eventDate()!!,
                            eventAndValue.second.value()!!.toFloat()
                        )
                    }

                    Graph(
                        "${programStage.displayName()}-${dataElement.displayFormName()}",
                        false,
                        coordinates,
                        "",
                        programStage.periodType() ?: PeriodType.Daily,
                        baseRepository.periodStep(programStage.periodType())
                    )
                }.filter { it.coordinates.isNotEmpty() }
            }.flatten()
    }
}