package dhis2.org.analytics.charts.providers

import dhis2.org.analytics.charts.data.GraphPoint
import java.text.SimpleDateFormat
import java.util.Date
import org.hisp.dhis.android.core.D2

class ChartCoordinatesProviderImpl(val d2: D2) : ChartCoordinatesProvider {

    override fun dataElementCoordinates(
        stageUid: String,
        teiUid: String,
        dataElementUid: String
    ): List<GraphPoint> {
        return d2.analyticsModule().eventLineList()
            .byProgramStage().eq(stageUid)
            .byTrackedEntityInstance().eq(teiUid)
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
    }

    override fun indicatorCoordinates(
        stageUid: String,
        teiUid: String,
        indicatorUid: String
    ): List<GraphPoint> {
        return d2.analyticsModule()
            .eventLineList()
            .byProgramStage().eq(stageUid)
            .byTrackedEntityInstance().eq(teiUid)
            .withProgramIndicator(indicatorUid)
            .blockingEvaluate()
            .sortedBy { it.date }
            .filter {
                try {
                    !(it.values.first().value?.toFloat() ?: Float.NaN).isNaN()
                } catch (e: java.lang.Exception) {
                    false
                }
            }
            .mapNotNull { lineListResponse ->
                lineListResponse.values.first().value?.let { value ->
                    GraphPoint(
                        eventDate = formattedDate(lineListResponse.date),
                        fieldValue = value.toFloat()
                    )
                }
            }
    }

    override fun nutritionCoordinates(
        stageUid: String,
        teiUid: String,
        zScoreValueContainerUid: String,
        zScoreSavedIsDataElement: Boolean,
        ageOrHeightCountainerUid: String,
        ageOrHeightIsDataElement: Boolean
    ): List<GraphPoint> {
        var eventLineListRepository = d2.analyticsModule().eventLineList()
            .byProgramStage().eq(stageUid)
            .byTrackedEntityInstance().eq(teiUid)
        eventLineListRepository = if (zScoreSavedIsDataElement) {
            eventLineListRepository.withDataElement(zScoreValueContainerUid)
        } else {
            eventLineListRepository.withProgramIndicator(zScoreValueContainerUid)
        }
        eventLineListRepository = if (ageOrHeightIsDataElement) {
            eventLineListRepository.withDataElement(ageOrHeightCountainerUid)
        } else {
            eventLineListRepository.withProgramIndicator(ageOrHeightCountainerUid)
        }
        return eventLineListRepository.blockingEvaluate().mapNotNull { lineListResponse ->
            val zScoreValue =
                lineListResponse.values.firstOrNull { it.uid == zScoreValueContainerUid }?.value
            val xAxisValue =
                lineListResponse.values.firstOrNull { it.uid == ageOrHeightCountainerUid }?.value
            if (zScoreValue == null || xAxisValue == null) {
                null
            } else {
                GraphPoint(
                    eventDate = formattedDate(lineListResponse.date),
                    position = xAxisValue.toFloat(),
                    fieldValue = zScoreValue.toFloat()
                )
            }
        }
    }

    private fun formattedDate(date: Date): Date {
        return try {
            val formattedDateString = SimpleDateFormat("yyyy-MM-dd").format(date)
            val formattedDate = SimpleDateFormat("yyyy-MM-dd").parse(formattedDateString)
            formattedDate ?: date
        } catch (e: Exception) {
            date
        }
    }
}
