package org.dhis2.usescases.datasets

import com.nhaarman.mockitokotlin2.mock
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.MapFieldValueToUser
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.TableDataToTableModelMapper
import org.junit.Before
import org.junit.Test

class TableDataToTableModelMapperTest {

    private val mapFieldValueToUser: MapFieldValueToUser = mock()
    private lateinit var tableDataToTableModelMapper: TableDataToTableModelMapper

    @Before
    fun setUp() {
        tableDataToTableModelMapper = TableDataToTableModelMapper(mapFieldValueToUser)
    }

    @Test
    fun `Should Map Indicators`() {
        val map = mapOf<String?, String>(
            Pair("Indicator 1", "2"),
            Pair("Indicator 2", "1"),
            Pair("Indicator 3", "3")
        )
        val sorted = map.toSortedMap { o1, o2 -> if (o1!! > o2!!) -1 else 1 }

        val result = tableDataToTableModelMapper.map(sorted)

        assert(result.tableRows.size == 3)
        assert(result.tableRows[0].rowHeader.title == "Indicator 3")
        assert(result.tableRows[0].values[0]!!.value == "3")

        assert(result.tableRows[1].rowHeader.title == "Indicator 2")
        assert(result.tableRows[1].values[0]!!.value == "1")

        assert(result.tableRows[2].rowHeader.title == "Indicator 1")
        assert(result.tableRows[2].values[0]!!.value == "2")
    }
}
