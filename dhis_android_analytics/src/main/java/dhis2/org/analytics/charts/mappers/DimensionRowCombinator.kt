package dhis2.org.analytics.charts.mappers

import org.hisp.dhis.android.core.analytics.aggregated.GridAnalyticsResponse

class DimensionRowCombinator {
    fun combineWithNextItem(
        gridAnalyticsResponse: GridAnalyticsResponse,
        currentList: MutableList<String>,
        currentValueIndex: Int = -1,
        currentValue: String? = null,
        hasMoreRows: Boolean = true
    ) {
        if (hasMoreRows) {
            val nextValueIndex = currentValueIndex + 1
            val isFinalRow = nextValueIndex == gridAnalyticsResponse.headers.rows.size - 1
            gridAnalyticsResponse.headers.rows[nextValueIndex].forEach {
                val newValue = gridAnalyticsResponse.metadata[it.id]?.displayName
                val nextValue = if (currentValueIndex == -1) {
                    newValue
                } else {
                    "$currentValue - $newValue"
                }
                combineWithNextItem(
                    gridAnalyticsResponse,
                    currentList,
                    nextValueIndex,
                    nextValue,
                    !isFinalRow
                )
            }
        } else {
            currentValue?.let { currentList.add(currentValue) }
        }
    }
}
