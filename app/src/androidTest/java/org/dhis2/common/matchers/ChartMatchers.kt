package org.dhis2.common.matchers

import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.compose.ui.node.RootForTest
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.test.espresso.matcher.BoundedMatcher
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
import dhis2.org.analytics.charts.data.ChartType
import org.dhis2.composetable.ui.semantics.CELL_VALUE_TEST_TAG
import org.hamcrest.Description
import org.hamcrest.Matcher

class ChartMatchers {
    companion object {
        fun hasChartType(
            @NonNull chartType: ChartType
        ): Matcher<View> {
            return object : BoundedMatcher<View, View>(View::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText("has item: ")
                }

                override fun matchesSafely(view: View): Boolean {
                    return when (chartType){
                        ChartType.LINE_CHART -> view is LineChart
                        ChartType.BAR_CHART -> view is BarChart
                        ChartType.TABLE, ChartType.LINE_LISTING ->
                            view is ComposeView && view.tableCellValues().isNotEmpty()
                        ChartType.SINGLE_VALUE -> view is ComposeView
                        ChartType.NUTRITION -> view is LineChart
                        ChartType.RADAR -> view is RadarChart
                        ChartType.PIE_CHART -> view is PieChart
                    }
                }
            }
        }

        fun hasChartData(entryCount: Int): Matcher<View> =
            object : BoundedMatcher<View, Chart<*>>(Chart::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText(
                        "chart with $entryCount entries per data set and a non-zero total",
                    )
                }

                override fun matchesSafely(chart: Chart<*>): Boolean {
                    val dataSets = chart.data?.dataSets.orEmpty()
                    val total =
                        dataSets.sumOf { dataSet ->
                            (0 until dataSet.entryCount).sumOf {
                                dataSet.getEntryForIndex(it).y.toDouble()
                            }
                        }
                    return dataSets.isNotEmpty() &&
                        dataSets.all { it.entryCount == entryCount } &&
                        total > 0
                }
            }

        // Matches a compose table that renders at least one value cell and no empty one.
        fun hasNoEmptyTableCell(): Matcher<View> =
            object : BoundedMatcher<View, ComposeView>(ComposeView::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText("table with values in every cell")
                }

                override fun matchesSafely(view: ComposeView): Boolean {
                    val values = view.tableCellValues()
                    return values.isNotEmpty() && values.none { it.isBlank() }
                }
            }

        // Matches a compose table that renders a value cell with exactly [text].
        fun hasTableCellWithText(text: String): Matcher<View> =
            object : BoundedMatcher<View, ComposeView>(ComposeView::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText("table with a cell showing \"$text\"")
                }

                override fun matchesSafely(view: ComposeView): Boolean =
                    view.tableCellValues().contains(text)
            }

        private fun View.tableCellValues(): List<String> =
            composeRoots()
                .flatMap { it.semanticsOwner.unmergedRootSemanticsNode.withDescendants() }
                .filter { it.config.getOrNull(SemanticsProperties.TestTag) == CELL_VALUE_TEST_TAG }
                .map { node ->
                    node.config
                        .getOrNull(SemanticsProperties.Text)
                        ?.joinToString(separator = "") { it.text }
                        .orEmpty()
                }

        private fun View.composeRoots(): List<RootForTest> =
            when {
                this is RootForTest -> listOf(this)
                this is ViewGroup -> (0 until childCount).flatMap { getChildAt(it).composeRoots() }
                else -> emptyList()
            }

        private fun SemanticsNode.withDescendants(): List<SemanticsNode> =
            listOf(this) + children.flatMap { it.withDescendants() }
    }
}
