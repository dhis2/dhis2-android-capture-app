package dhis2.org.analytics.charts.mappers

import org.hisp.dhis.android.core.analytics.aggregated.GridAnalyticsResponse

class DimensionRowCombinator {
    fun combinedRow(gridAnalyticsResponse: GridAnalyticsResponse): List<String> {
        var combinedList = mutableListOf<String>()
        gridAnalyticsResponse.headers.rows.forEach { category ->
            val categoryList = mutableListOf<String>()
            category.forEach { gridHeaderItem ->
                val itemName = gridAnalyticsResponse.metadata[gridHeaderItem.id]?.displayName
                repeat(gridHeaderItem.weight) {
                    itemName?.let { element -> categoryList.add(element) }
                }
            }
            combinedList =
                if (combinedList.isEmpty()) {
                    categoryList
                } else {
                    combinedList.zip(categoryList) { x, y -> "$x - $y" }.toMutableList()
                }
        }
        return combinedList
    }
}
