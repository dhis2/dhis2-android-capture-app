package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import junit.framework.Assert.assertTrue
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Test
import java.time.Instant
import java.util.Date

class GraphCoordinatesToEntryTest {

    private val graphToLineData = GraphCoordinatesToEntry()
    private var dailyPeriodPeriod: Long =
        Date.from(Instant.parse("2020-01-02T00:00:00.00Z")).time - Date.from(Instant.parse("2020-01-01T00:00:00.00Z")).time

    @Test
    fun `Should return mapped list`() {
        val result = graphToLineData.map(mockedGraph())
        val expectedEntryPosition = listOf(0f, 1f, 3f, 6f)
        assertTrue(result.size == 4)
        result.forEachIndexed { index, entry ->
            assertTrue(
                entry.y == mockedCoordinates()[index].fieldValue &&
                        entry.x == expectedEntryPosition[index]
            )
        }
    }

    @Test
    fun `Should return empty mapped list`() {
        val result = graphToLineData.map(mockedGraph(emptyList()))
        assertTrue(result.isEmpty())
    }

    private fun mockedGraph(coordinates: List<GraphPoint>? = mockedCoordinates()): Graph {
        return Graph(
            "testGraph",
            false,
            coordinates!!,
            "periodToDisplay",
            PeriodType.Daily,
            dailyPeriodPeriod
        )
    }

    private fun mockedCoordinates(): List<GraphPoint> {
        return arrayListOf(
            GraphPoint(Date.from(Instant.parse("2020-01-01T00:00:00.00Z")), 10f),
            GraphPoint(Date.from(Instant.parse("2020-01-02T00:00:00.00Z")), 20f),
            GraphPoint(Date.from(Instant.parse("2020-01-04T00:00:00.00Z")), 50f),
            GraphPoint(Date.from(Instant.parse("2020-01-07T00:00:00.00Z")), 30f)
        )
    }
}