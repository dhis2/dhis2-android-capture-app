package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphFieldValue
import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.SerieData
import junit.framework.Assert.assertTrue
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Test
import java.time.Instant
import java.util.Date

class GraphCoordinatesToEntryTest {

    private val graphToLineData = GraphCoordinatesToEntry()
    private var dailyPeriodPeriod: Long =
        Date.from(Instant.parse("2020-01-02T00:00:00.00Z")).time -
            Date.from(Instant.parse("2020-01-01T00:00:00.00Z")).time

    @Test
    fun `Should return mapped list`() {
        val result = graphToLineData.map(mockedGraph(), mockedCoordinates(), "serieLabel")
        val expectedEntryPosition = listOf(0f, 0.032258064f, 0.09677419f, 0.19354838f)
        assertTrue(result.size == 4)
        result.forEachIndexed { index, entry ->
            assertTrue(
                entry.y == mockedCoordinates()[index].numericValue() &&
                    entry.x == expectedEntryPosition[index],
            )
        }
    }

    @Test
    fun `Should return empty mapped list`() {
        val result = graphToLineData.map(mockedGraph(emptyList()), emptyList(), "serie Label")
        assertTrue(result.isEmpty())
    }

    private fun mockedGraph(coordinates: List<GraphPoint> = mockedCoordinates()): Graph {
        return Graph(
            "testGraph",
            coordinates.map { SerieData("fieldName", coordinates) },
            null,
            PeriodType.Daily,
            dailyPeriodPeriod,
        )
    }

    private fun mockedCoordinates(): List<GraphPoint> {
        return arrayListOf(
            GraphPoint(Date.from(Instant.parse("2020-01-01T00:00:00.00Z")), null, GraphFieldValue.Numeric(10f)),
            GraphPoint(Date.from(Instant.parse("2020-01-02T00:00:00.00Z")), null, GraphFieldValue.Numeric(20f)),
            GraphPoint(Date.from(Instant.parse("2020-01-04T00:00:00.00Z")), null, GraphFieldValue.Numeric(50f)),
            GraphPoint(Date.from(Instant.parse("2020-01-07T00:00:00.00Z")), null, GraphFieldValue.Numeric(30f)),
        )
    }
}
