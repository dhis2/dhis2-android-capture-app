package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

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
                            formattedDate(eventAndValue.first.eventDate()!!),
                            eventAndValue.second.value()!!.toFloat()
                        )
                    }

                    Graph(
                        "${programStage.periodType()?.name?:PeriodType.Daily.name}-${dataElement.displayFormName()}",
                        false,
                        coordinates,
                        "",
                        programStage.periodType() ?: PeriodType.Daily,
                        baseRepository.periodStep(programStage.periodType())
                    )
                }.filter { it.coordinates.isNotEmpty() }
            }.flatten()
    }

    private fun formattedDate(date: Date):Date{
        return try {
            val formattedDateString = SimpleDateFormat("yyyy-MM-dd").format(date)
            val formattedDate = SimpleDateFormat("yyyy-MM-dd").parse(formattedDateString)
            formattedDate?:date
        }catch (e:Exception){
            date
        }
    }
}