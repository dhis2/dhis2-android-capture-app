package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieColors
import dhis2.org.analytics.charts.data.SerieData
import org.hisp.dhis.android.core.period.PeriodType

const val DAILY_BAR_WIDTH = 1f / 30f
const val WEEKLY_BAR_WIDTH = 0.2f
const val MONTHLY_BAR_WIDTH = 0.75f
const val SIX_MONTHLY_BAR_WIDTH = 0.85f
const val YEARLY_BAR_WIDTH = 0.85f
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
                DAILY_BAR_WIDTH
            PeriodType.Weekly,
            PeriodType.WeeklySaturday,
            PeriodType.WeeklySunday,
            PeriodType.WeeklyThursday,
            PeriodType.WeeklyWednesday ->
                WEEKLY_BAR_WIDTH
            PeriodType.BiWeekly,
            PeriodType.Monthly,
            PeriodType.BiMonthly ->
                MONTHLY_BAR_WIDTH
            PeriodType.Quarterly,
            PeriodType.SixMonthly,
            PeriodType.SixMonthlyApril,
            PeriodType.SixMonthlyNov -> {
                SIX_MONTHLY_BAR_WIDTH
            }
            PeriodType.Yearly,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct,
            PeriodType.FinancialNov -> {
                YEARLY_BAR_WIDTH
            }
        }
    }
}
