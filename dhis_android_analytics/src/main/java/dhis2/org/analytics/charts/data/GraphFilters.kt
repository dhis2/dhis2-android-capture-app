package dhis2.org.analytics.charts.data

import org.hisp.dhis.android.core.common.RelativePeriod

sealed class GraphFilters {
    abstract fun count(): Int

    abstract fun canDisplayChart(dataIsNotEmpty: Boolean): Boolean

    data class Visualization(
        val orgUnitsDefault: List<String> = emptyList(),
        val orgUnitsSelected: List<String> = emptyList(),
        val periodToDisplaySelected: RelativePeriod? = null,
    ) : GraphFilters() {
        override fun count(): Int {
            var count = 0
            if (orgUnitsSelected.isNotEmpty()) count++
            if (periodToDisplaySelected != null) count++
            return count
        }

        override fun canDisplayChart(dataIsNotEmpty: Boolean): Boolean {
            return if (orgUnitsSelected.isNotEmpty() || periodToDisplaySelected != null) {
                true
            } else {
                dataIsNotEmpty
            }
        }
    }

    data class LineListing(
        val lineListFilters: Map<Int, String> = emptyMap(),
        val orgUnitsSelected: Map<Int, List<String>> = emptyMap(),
        val periodToDisplaySelected: Map<Int, List<RelativePeriod>> = emptyMap(),
    ) : GraphFilters() {

        fun columnsWithFilters() =
            (lineListFilters.keys + orgUnitsSelected.keys + periodToDisplaySelected.keys).distinct()

        fun hasFilters() =
            lineListFilters.isNotEmpty() || orgUnitsSelected.isNotEmpty() || periodToDisplaySelected.isNotEmpty()

        override fun count(): Int {
            var count = 0
            if (hasFilters()) count++
            return count
        }

        override fun canDisplayChart(dataIsNotEmpty: Boolean): Boolean {
            return dataIsNotEmpty
        }
    }
}
