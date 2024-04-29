package dhis2.org.analytics.charts.data

import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.Date

class GraphTest {

    private var dailyPeriodPeriod: Long =
        Date.from(Instant.parse("2020-01-02T00:00:00.00Z")).time -
            Date.from(Instant.parse("2020-01-01T00:00:00.00Z")).time

    @Test
    fun `Should return the correct number of steps from the first one`() {
        val graph = mockedGraph()
        val expectedResult = listOf(0f, 1f, 3f, 6f)
        mockedCoordinates().forEachIndexed { index, graphPoint ->
            assertTrue(graph.numberOfStepsToDate(graphPoint.eventDate) == expectedResult[index])
        }
    }

    @Test
    fun `Should return the correct number of steps if empty coordinates`() {
        val graph = mockedGraph(emptyList())
        val testDate = Date.from(Instant.parse("2020-01-02T00:00:00.00Z"))
        assertTrue(graph.numberOfStepsToDate(testDate) == 0f)
    }

    @Test
    fun `Should return the correct date from n steps`() {
        val graph = mockedGraph()
        val testSteps = listOf(1L, 3L, 5L, 6L)
        val expectedResult = listOf(
            Date.from(Instant.parse("2020-01-02T00:00:00.00Z")),
            Date.from(Instant.parse("2020-01-04T00:00:00.00Z")),
            Date.from(Instant.parse("2020-01-06T00:00:00.00Z")),
            Date.from(Instant.parse("2020-01-07T00:00:00.00Z")),
        )
        testSteps.forEachIndexed { index, step ->
            assertTrue(graph.dateFromSteps(step) == expectedResult[index])
        }
    }

    @Test
    fun `Should return the correct date from n steps if empty coordinates`() {
        val graph = mockedGraph(emptyList())
        assertTrue(graph.dateFromSteps(5L) == null)
    }

    @Test
    fun `Should return the maximum`() {
        val graph = mockedGraph()
        assertTrue(graph.maxValue() == 50f)
    }

    @Test
    fun `Should return null maximum if empty coordinate`() {
        val graph = mockedGraph(emptyList())
        assertTrue(graph.maxValue() == 0f)
    }

    @Test
    fun `Should return the correct minimum`() {
        val graph = mockedGraph()
        assertTrue(graph.minValue() == 10f)
    }

    @Test
    fun `Should return null minimum if empty coordinate`() {
        val graph = mockedGraph(emptyList())
        assertTrue(graph.minValue() == 0f)
    }

    @Test
    fun `Should xAxixMaximun catch exception if there are series with empty coordinates`() {
        val graph = mockedGraph(emptyList())
        assertTrue(graph.xAxixMaximun() == 0.0f)
    }

    @Test(expected = Test.None::class)
    fun `Should use current date to calculate localDateFromSteps and don't throw exception`() {
        val graph = mockedGraph(emptyList())
        graph.localDateFromSteps(3)
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
            GraphPoint(Date.from(Instant.parse("2020-01-01T00:00:00.00Z")), fieldValue = GraphFieldValue.Numeric(10f)),
            GraphPoint(Date.from(Instant.parse("2020-01-02T00:00:00.00Z")), fieldValue = GraphFieldValue.Numeric(20f)),
            GraphPoint(Date.from(Instant.parse("2020-01-04T00:00:00.00Z")), fieldValue = GraphFieldValue.Numeric(50f)),
            GraphPoint(Date.from(Instant.parse("2020-01-07T00:00:00.00Z")), fieldValue = GraphFieldValue.Numeric(30f)),
        )
    }
}
