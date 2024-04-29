package dhis2.org.analytics.charts.mappers

import junit.framework.Assert.assertTrue
import org.hisp.dhis.android.core.analytics.aggregated.GridHeader
import org.hisp.dhis.android.core.analytics.aggregated.GridHeaderItem
import org.hisp.dhis.android.core.analytics.aggregated.MetadataItem
import org.hisp.dhis.android.core.analytics.aggregated.mock.GridAnalyticsResponseSamples
import org.hisp.dhis.android.core.category.CategoryOption
import org.junit.Test

class DimensionRowCombinatorTest {

    private val dimensionRowCombinator = DimensionRowCombinator()

    @Test
    fun shouldCombineRows() {
        val finalList = mutableListOf<String>()
        dimensionRowCombinator.combineWithNextItem(
            gridAnalyticsResponse = testGridAnalyticsResponse(),
            currentList = finalList,
        )

        assertTrue(
            finalList.size == 12,
        )
        assertTrue(
            finalList[0] == "A - 1 - C",
        )
        assertTrue(
            finalList[5] == "A - 3 - D",
        )
        assertTrue(
            finalList[8] == "B - 2 - C",
        )
        assertTrue(
            finalList[11] == "B - 3 - D",
        )
    }

    @Test
    fun shouldNotCombineIfThereAreNoRows() {
        val finalList = mutableListOf<String>()
        dimensionRowCombinator.combineWithNextItem(
            gridAnalyticsResponse = testGridAnalyticsResponse(emptyList()),
            currentList = finalList,
            hasMoreRows = false,
        )

        assertTrue(
            finalList.isEmpty(),
        )
    }

    private fun testGridAnalyticsResponse(testingRows: List<List<GridHeaderItem>> = testingRows()) =
        GridAnalyticsResponseSamples.sample1.copy(
            metadata = GridAnalyticsResponseSamples.sample1.metadata.toMutableMap().apply {
                putAll(
                    mapOf(
                        row11.uid() to MetadataItem.CategoryOptionItem(row11),
                        row12.uid() to MetadataItem.CategoryOptionItem(row12),
                        row21.uid() to MetadataItem.CategoryOptionItem(row21),
                        row22.uid() to MetadataItem.CategoryOptionItem(row22),
                        row23.uid() to MetadataItem.CategoryOptionItem(row23),
                        row31.uid() to MetadataItem.CategoryOptionItem(row31),
                        row32.uid() to MetadataItem.CategoryOptionItem(row32),
                    ),
                )
            },
            headers = GridHeader(
                columns = GridAnalyticsResponseSamples.sample1.headers.columns,
                rows = testingRows,
            ),
        )

    private fun testingRows() = listOf(
        listOf(
            GridHeaderItem(row11.uid(), 6),
            GridHeaderItem(row12.uid(), 6),
        ),
        listOf(
            GridHeaderItem(row21.uid(), 2),
            GridHeaderItem(row22.uid(), 2),
            GridHeaderItem(row23.uid(), 2),
        ),
        listOf(
            GridHeaderItem(row31.uid(), 1),
            GridHeaderItem(row32.uid(), 1),
        ),
    )

    val row11 = CategoryOption.builder()
        .uid("row11")
        .displayName("A")
        .build()
    val row12 = CategoryOption.builder()
        .uid("row12")
        .displayName("B")
        .build()
    val row21 = CategoryOption.builder()
        .uid("row21")
        .displayName("1")
        .build()
    val row22 = CategoryOption.builder()
        .uid("row22")
        .displayName("2")
        .build()
    val row23 = CategoryOption.builder()
        .uid("row23")
        .displayName("3")
        .build()
    val row31 = CategoryOption.builder()
        .uid("row31")
        .displayName("C")
        .build()
    val row32 = CategoryOption.builder()
        .uid("row32")
        .displayName("D")
        .build()
}
