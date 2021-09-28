package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.SerieData
import java.util.GregorianCalendar
import org.hisp.dhis.android.core.analytics.aggregated.Dimension
import org.hisp.dhis.android.core.analytics.aggregated.DimensionalResponse

class DimensionalResponseToPieData {
    fun map(dimensionalResponse: DimensionalResponse, dimension: Dimension): List<SerieData> {
        val dimensionIndex = dimensionalResponse.dimensions.indexOf(dimension)
        val coordinates = dimensionalResponse.values.groupBy { it.dimensions[dimensionIndex] }
            .map { entry ->
                GraphPoint(
                    eventDate = GregorianCalendar(2021, 0, 1).time,
                    fieldValue = entry.value.sumByDouble { it.value?.toDouble() ?: 0.0 }.toFloat(),
                    legend = dimensionalResponse.metadata[entry.key]?.displayName
                )
            }
        return listOf(SerieData("", coordinates))
    }
}
