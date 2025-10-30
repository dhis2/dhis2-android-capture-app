package dhis2.org.analytics.charts.bindings

import org.hisp.dhis.android.core.analytics.trackerlinelist.DataFilter
import org.hisp.dhis.android.core.analytics.trackerlinelist.TrackerLineListItem
import org.junit.Test

class LineListingExtensionsTest {
    @Test
    fun `should add a filter to a Category LineListingItem`() {
        val item =
            TrackerLineListItem.Category(
                uid = "uid1",
                filters = listOf(),
            )

        assert(item.filters.isEmpty())

        val result = item.withFilters("categoryDisplayName", listOf("categoryUid"))

        assert((result as TrackerLineListItem.Category).filters.size == 1)
        assert((result.filters.first() as DataFilter.In).values.isNotEmpty())
    }
}
