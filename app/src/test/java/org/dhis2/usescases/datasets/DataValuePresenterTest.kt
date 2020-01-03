package org.dhis2.usescases.datasets

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel
import org.dhis2.data.forms.dataentry.tablefields.edittext.EditTextViewModel
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataValueContract
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataValuePresenter
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataValueRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Before
import org.junit.Test

class DataValuePresenterTest {

    private lateinit var presenter: DataValuePresenter

    private val view: DataValueContract.View = mock()
    private val dataValueRepository: DataValueRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val analyticsHelper: AnalyticsHelper = mock()

    @Before
    fun setup() {
        presenter = DataValuePresenter(view, dataValueRepository, schedulers, analyticsHelper)
    }

    @Test
    fun `Check all row have values`() {
        val dataValues: List<DataSetTableModel> = createDataValues()

        val tableCells: MutableList<List<List<FieldViewModel>>> = createTableCells()

        assertTrue(presenter.checkAllFieldRequired(tableCells, dataValues))
    }

    @Test
    fun `Check no row have values`() {
        val dataValues: List<DataSetTableModel> = listOf()

        val tableCells: MutableList<List<List<FieldViewModel>>> = createTableCells()

        assertTrue(presenter.checkAllFieldRequired(tableCells, dataValues))
    }

    @Test
    fun `Check one field without value`() {
        val dataValues: MutableList<DataSetTableModel> = createDataValues().toMutableList()

        dataValues.removeAt(0)

        val tableCells: MutableList<List<List<FieldViewModel>>> = createTableCells()

        assertFalse(presenter.checkAllFieldRequired(tableCells, dataValues))
        verify(view).highligthHeaderRow(0, 0, false)
    }

    @Test
    fun `Check all mandatory fields with value`() {
        val dataValues: List<DataSetTableModel> = createDataValues()

        val tableCells: MutableList<List<List<FieldViewModel>>> = createTableCells()

        assertTrue(presenter.checkMandatoryField(tableCells, dataValues))
    }

    @Test
    fun `Check mandatory field without value`() {
        val dataValues: MutableList<DataSetTableModel> = createDataValues().toMutableList()

        dataValues.removeAt(0)

        val tableCells: MutableList<List<List<FieldViewModel>>> = createTableCells()

        assertFalse(presenter.checkMandatoryField(tableCells, dataValues))
        verify(view).highligthHeaderRow(0, 0, true)
    }

    private fun createDataValues(): List<DataSetTableModel> {
        val dataValues = arrayListOf<DataSetTableModel>()
        repeat(2) { row ->
            repeat(2) { column ->
                dataValues.add(
                    DataSetTableModel.create(
                        0,
                        "$row",
                        "",
                        "",
                        "$column",
                        "",
                        "value",
                        "",
                        null,
                        null,
                        null
                    )
                )
            }
        }
        return dataValues
    }

    private fun createTableCells(): MutableList<List<List<FieldViewModel>>> {
        val table = arrayListOf<List<FieldViewModel>>()
        repeat(2) { row ->
            val fields = arrayListOf<FieldViewModel>()
            repeat(2) { column ->
                fields.add(
                    EditTextViewModel.create(
                        "",
                        "",
                        true,
                        "",
                        "",
                        1,
                        ValueType.TEXT,
                        "",
                        true,
                        "",
                        "$row",
                        listOf(),
                        "",
                        row,
                        column,
                        "$column",
                        ""
                    )

                )
            }
            table.add(fields)
        }
        return mutableListOf(table)
    }
}
