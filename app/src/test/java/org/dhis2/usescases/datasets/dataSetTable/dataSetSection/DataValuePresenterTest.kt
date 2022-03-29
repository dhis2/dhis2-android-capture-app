package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import com.nhaarman.mockitokotlin2.mock
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel
import org.dhis2.data.forms.dataentry.tablefields.edittext.EditTextViewModel
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Before

class DataValuePresenterTest {

    private lateinit var presenter: DataValuePresenter

    private val view: DataValueContract.View = mock()
    private val dataValueRepository: DataValueRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val valueStore: ValueStore = mock()
    private val updateProcessor: FlowableProcessor<Unit> = PublishProcessor.create()

    @Before
    fun setup() {
        presenter =
            DataValuePresenter(
                view,
                dataValueRepository,
                valueStore,
                schedulers,
                updateProcessor
            )
    }

    private fun createDataValues(): List<DataSetTableModel> {
        val dataValues = arrayListOf<DataSetTableModel>()
        repeat(2) { row ->
            repeat(2) { column ->
                dataValues.add(
                    DataSetTableModel(
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
