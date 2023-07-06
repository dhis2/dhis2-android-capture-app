package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieColors
import dhis2.org.analytics.charts.data.SerieData
import org.hisp.dhis.android.core.period.PeriodType

class GraphToBarData {
    private val coordinateToBarEntryMapper by lazy { GraphCoordinatesToBarEntry() }
    private val serieColors = SerieColors.getColors()

    fun map(graph: Graph, serieToHighlight: String? = null): BarData {
        val series = if (graph.chartType == ChartType.NUTRITION) {
            listOf(graph.series.last())
        } else {
            graph.series
        }
        return BarData(
            series.mapIndexed { index: Int, serie: SerieData ->
                BarDataSet(
                    coordinateToBarEntryMapper.map(
                        graph,
                        serie.coordinates,
                        index,
                        series.size,
                        serie.fieldName
                    ),
                    serie.fieldName
                ).apply {
                    val colorIndex = index % serieColors.size
                    val isHighlighted = serieToHighlight == null || label == serieToHighlight
                    val serieColor = SerieColors.getSerieColor(colorIndex, isHighlighted)
                    val singleSerie =
                        graph.series.filter { !it.coordinates.isNullOrEmpty() }.size == 1
                    setDrawValues(singleSerie || label == serieToHighlight)
                    color = serieColor
                }
            }
        ).withGlobalStyle(barWidthByPeriod(graph))
    }

    private fun barWidthByPeriod(graph: Graph): Float? {
        return when (graph.eventPeriodType) {
            PeriodType.Daily ->
                1f / 30f
            PeriodType.Weekly,
            PeriodType.WeeklySaturday,
            PeriodType.WeeklySunday,
            PeriodType.WeeklyThursday,
            PeriodType.WeeklyWednesday ->
                0.2f
            PeriodType.BiWeekly,
            PeriodType.Monthly,
            PeriodType.BiMonthly ->
                0.75f
            PeriodType.Quarterly,
            PeriodType.SixMonthly,
            PeriodType.SixMonthlyApril,
            PeriodType.SixMonthlyNov -> {
                0.85f
            }
            PeriodType.Yearly,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct,
            PeriodType.FinancialNov -> {
                0.85f
            }
        }
    }
}
