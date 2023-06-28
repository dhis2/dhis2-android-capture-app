package org.dhis2.usescases.datasets

import com.nhaarman.mockitokotlin2.mock
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel
import org.dhis2.data.forms.dataentry.tablefields.edittext.EditTextViewModel
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataTableModel
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.MapFieldValueToUser
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.TableData
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.TableDataToTableModelMapper
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

const val DATAELEMENT_FORM_NAME = "dataElement_formName"
const val DATAELEMENT_DESCRIPTION = "dataElement_description"

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

    @Test
    fun `Should Map Table Data`() {
        val dataTableModel = DataTableModel(
            "123",
            "Madrid",
            "none",
            listOf(getDataElement(), getDataElement()).toMutableList(),
            null,
            null,
            null,
            null,
            null,
            null
        )

        val tableData = TableData(
            dataTableModel,
            listOf(listOf(getElements(), getElements()), listOf(getElements(), getElements())),
            emptyList(),
            true,
            showRowTotals = false,
            showColumnTotals = false,
            hasDataElementDecoration = false
        )

        val result = tableDataToTableModelMapper(tableData)
        result.tableRows
        assert(result.tableRows[0].rowHeader.title == DATAELEMENT_FORM_NAME)
        assert(result.tableRows[1].rowHeader.title == DATAELEMENT_FORM_NAME)
        assert(result.tableRows[0].values.size == 2)
        assert(result.tableRows[1].values.size == 2)
    }

    @Test
    fun `Should mark all cell as no editable if write access is false`() {
        val dataTableModel = DataTableModel(
            "123",
            "Madrid",
            "none",
            listOf(getDataElement(), getDataElement()).toMutableList(),
            null,
            null,
            null,
            null,
            null,
            null
        )

        val tableData = TableData(
            dataTableModel,
            listOf(listOf(getElements(), getElements()), listOf(getElements(), getElements())),
            emptyList(),
            false,
            showRowTotals = false,
            showColumnTotals = false,
            hasDataElementDecoration = false
        )

        val result = tableDataToTableModelMapper(tableData)

        result.tableRows.forEach { row ->
            row.values.values.forEach { cell ->
                assertFalse(cell.editable)
            }
        }
    }

    private fun getElements(): FieldViewModel {
        return EditTextViewModel.create(
            "",
            "",
            true,
            "True",
            "",
            1,
            ValueType.BOOLEAN,
            "",
            true,
            "",
            "none",
            listOf(),
            "",
            0,
            1,
            "1",
            ""
        )
    }

    private fun getDataElement(): DataElement {
        return DataElement.builder().uid(DATAELEMENT_UID).valueType(ValueType.BOOLEAN)
            .displayFormName(DATAELEMENT_FORM_NAME)
            .displayDescription(DATAELEMENT_DESCRIPTION).build()
    }
}
