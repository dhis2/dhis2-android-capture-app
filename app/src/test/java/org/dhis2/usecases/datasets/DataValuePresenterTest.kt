package org.dhis2.usecases.datasets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel
import org.dhis2.data.forms.dataentry.tablefields.edittext.EditTextModel
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

    companion object{
        const val CELLS ="[[[{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"YBhrfw1dP2J\",\"column\":0,\"dataElement\":\"ZwrIPRUiHEB\",\"description\":\"\\u003c15y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: currently on care\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":0,\"storeBy\":\"android\",\"uid\":\"9021\",\"value\":\"19\",\"valueType\":\"INTEGER_POSITIVE\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"u5fU9rr67xo\",\"column\":1,\"dataElement\":\"ZwrIPRUiHEB\",\"description\":\"15-24y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: currently on care\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":0,\"storeBy\":\"android\",\"uid\":\"9022\",\"value\":\"27\",\"valueType\":\"INTEGER_POSITIVE\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"LbkJRbDblhe\",\"column\":2,\"dataElement\":\"ZwrIPRUiHEB\",\"description\":\"25-49y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: currently on care\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":0,\"storeBy\":\"android\",\"uid\":\"9023\",\"value\":\"22\",\"valueType\":\"INTEGER_POSITIVE\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"z858fbdqWwF\",\"column\":3,\"dataElement\":\"ZwrIPRUiHEB\",\"description\":\"\\u003e49y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: currently on care\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":0,\"storeBy\":\"android\",\"uid\":\"9024\",\"value\":\"52\",\"valueType\":\"INTEGER_POSITIVE\"}],[{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"YBhrfw1dP2J\",\"column\":0,\"dataElement\":\"veW7w0xDDOQ\",\"description\":\"\\u003c15y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: new on care\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":1,\"storeBy\":\"android\",\"uid\":\"9045\",\"value\":\"22\",\"valueType\":\"INTEGER_ZERO_OR_POSITIVE\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"u5fU9rr67xo\",\"column\":1,\"dataElement\":\"veW7w0xDDOQ\",\"description\":\"15-24y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: new on care\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":1,\"storeBy\":\"android\",\"uid\":\"9046\",\"value\":\"43\",\"valueType\":\"INTEGER_ZERO_OR_POSITIVE\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"LbkJRbDblhe\",\"column\":2,\"dataElement\":\"veW7w0xDDOQ\",\"description\":\"25-49y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: new on care\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":1,\"storeBy\":\"android\",\"uid\":\"9047\",\"value\":\"36\",\"valueType\":\"INTEGER_ZERO_OR_POSITIVE\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"z858fbdqWwF\",\"column\":3,\"dataElement\":\"veW7w0xDDOQ\",\"description\":\"\\u003e49y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: new on care\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":1,\"storeBy\":\"android\",\"uid\":\"9048\",\"value\":\"27\",\"valueType\":\"INTEGER_ZERO_OR_POSITIVE\"}],[{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"YBhrfw1dP2J\",\"column\":0,\"dataElement\":\"R4KStuS8qt7\",\"description\":\"\\u003c15y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: testing\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":2,\"storeBy\":\"android\",\"uid\":\"9029\",\"value\":\"49\",\"valueType\":\"NUMBER\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"u5fU9rr67xo\",\"column\":1,\"dataElement\":\"R4KStuS8qt7\",\"description\":\"15-24y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: testing\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":2,\"storeBy\":\"android\",\"uid\":\"9030\",\"value\":\"42\",\"valueType\":\"NUMBER\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"LbkJRbDblhe\",\"column\":2,\"dataElement\":\"R4KStuS8qt7\",\"description\":\"25-49y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: testing\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":2,\"storeBy\":\"android\",\"uid\":\"9031\",\"value\":\"23\",\"valueType\":\"NUMBER\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"z858fbdqWwF\",\"column\":3,\"dataElement\":\"R4KStuS8qt7\",\"description\":\"\\u003e49y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: testing\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":2,\"storeBy\":\"android\",\"uid\":\"9032\",\"value\":\"36\",\"valueType\":\"NUMBER\"}],[{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"YBhrfw1dP2J\",\"column\":0,\"dataElement\":\"o0fOD1HLuv8\",\"description\":\"\\u003c15y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: counseling\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":3,\"storeBy\":\"android\",\"uid\":\"9037\",\"value\":\"23\",\"valueType\":\"INTEGER_POSITIVE\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"u5fU9rr67xo\",\"column\":1,\"dataElement\":\"o0fOD1HLuv8\",\"description\":\"15-24y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: counseling\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":3,\"storeBy\":\"android\",\"uid\":\"9038\",\"value\":\"24\",\"valueType\":\"INTEGER_POSITIVE\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"LbkJRbDblhe\",\"column\":2,\"dataElement\":\"o0fOD1HLuv8\",\"description\":\"25-49y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: counseling\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":3,\"storeBy\":\"android\",\"uid\":\"9039\",\"value\":\"36\",\"valueType\":\"INTEGER_POSITIVE\"},{\"catCombo\":\"Wfan7UwK8CQ\",\"categoryOptionCombo\":\"z858fbdqWwF\",\"column\":3,\"dataElement\":\"o0fOD1HLuv8\",\"description\":\"\\u003e49y\",\"editable\":false,\"hint\":\"\",\"inputType\":1,\"label\":\"HIV: counseling\",\"listCategoryOption\":[],\"mandatory\":false,\"maxLines\":1,\"programStageSection\":\"HIV testing and counseling\",\"row\":3,\"storeBy\":\"android\",\"uid\":\"9040\",\"value\":\"49\",\"valueType\":\"INTEGER_POSITIVE\"}]]]"
        const val DATAVALUES = "[{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"YBhrfw1dP2J\",\"dataElement\":\"ZwrIPRUiHEB\",\"id\":9021,\"listCategoryOption\":[\"zs2Ra6sikup\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"19\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"u5fU9rr67xo\",\"dataElement\":\"ZwrIPRUiHEB\",\"id\":9022,\"listCategoryOption\":[\"UOqJW6HPvvL\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"27\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"LbkJRbDblhe\",\"dataElement\":\"ZwrIPRUiHEB\",\"id\":9023,\"listCategoryOption\":[\"WAl0OCcIYxr\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"22\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"z858fbdqWwF\",\"dataElement\":\"ZwrIPRUiHEB\",\"id\":9024,\"listCategoryOption\":[\"IG8vRsn0HNl\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"52\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"YBhrfw1dP2J\",\"dataElement\":\"veW7w0xDDOQ\",\"id\":9045,\"listCategoryOption\":[\"zs2Ra6sikup\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"22\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"u5fU9rr67xo\",\"dataElement\":\"veW7w0xDDOQ\",\"id\":9046,\"listCategoryOption\":[\"UOqJW6HPvvL\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"43\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"LbkJRbDblhe\",\"dataElement\":\"veW7w0xDDOQ\",\"id\":9047,\"listCategoryOption\":[\"WAl0OCcIYxr\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"36\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"z858fbdqWwF\",\"dataElement\":\"veW7w0xDDOQ\",\"id\":9048,\"listCategoryOption\":[\"IG8vRsn0HNl\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"27\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"YBhrfw1dP2J\",\"dataElement\":\"R4KStuS8qt7\",\"id\":9029,\"listCategoryOption\":[\"zs2Ra6sikup\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"49\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"u5fU9rr67xo\",\"dataElement\":\"R4KStuS8qt7\",\"id\":9030,\"listCategoryOption\":[\"UOqJW6HPvvL\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"42\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"LbkJRbDblhe\",\"dataElement\":\"R4KStuS8qt7\",\"id\":9031,\"listCategoryOption\":[\"WAl0OCcIYxr\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"23\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"z858fbdqWwF\",\"dataElement\":\"R4KStuS8qt7\",\"id\":9032,\"listCategoryOption\":[\"IG8vRsn0HNl\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"36\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"YBhrfw1dP2J\",\"dataElement\":\"o0fOD1HLuv8\",\"id\":9037,\"listCategoryOption\":[\"zs2Ra6sikup\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"23\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"u5fU9rr67xo\",\"dataElement\":\"o0fOD1HLuv8\",\"id\":9038,\"listCategoryOption\":[\"UOqJW6HPvvL\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"24\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"LbkJRbDblhe\",\"dataElement\":\"o0fOD1HLuv8\",\"id\":9039,\"listCategoryOption\":[\"WAl0OCcIYxr\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"36\"},{\"attributeOptionCombo\":\"HllvX50cXC0\",\"catCombo\":\"Wfan7UwK8CQ\",\"catOption\":\"\",\"categoryOptionCombo\":\"z858fbdqWwF\",\"dataElement\":\"o0fOD1HLuv8\",\"id\":9040,\"listCategoryOption\":[\"IG8vRsn0HNl\"],\"organisationUnit\":\"DiszpKrYNg8\",\"period\":\"201811\",\"storedBy\":\"\",\"value\":\"49\"}]"
    }

    @Before
    fun setup() {
        presenter = DataValuePresenter(view, dataValueRepository, schedulers, analyticsHelper)
    }

    @Test
    fun `Check all row have values`() {

        val dataValues: List<DataSetTableModel> = createDataValues()
            //Gson().fromJson(DATAVALUES, object : TypeToken<List<DataSetTableModel>>() {}.type)

        val tableCells: MutableList<List<List<FieldViewModel>>> = createTableCells()
            //Gson().fromJson(CELLS, object : TypeToken<MutableList<List<List<FieldViewModel>>>>() {}.type)


        assertTrue(presenter.checkAllFieldRequired(tableCells, dataValues))
    }

    @Test
    fun `Check one field without value`() {

        val dataValues: MutableList<DataSetTableModel> = createDataValues().toMutableList()
        //Gson().fromJson(DATAVALUES, object : TypeToken<List<DataSetTableModel>>() {}.type)

        dataValues.removeAt(0)

        val tableCells: MutableList<List<List<FieldViewModel>>> = createTableCells()
        //Gson().fromJson(CELLS, object : TypeToken<MutableList<List<List<FieldViewModel>>>>() {}.type)


        assertFalse(presenter.checkAllFieldRequired(tableCells, dataValues))
    }

    private fun createDataValues() : List<DataSetTableModel>{
        val dataValues = arrayListOf<DataSetTableModel>()
        repeat(2){ row ->
            repeat(2){ column ->
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

    private fun createTableCells() : MutableList<List<List<FieldViewModel>>>{
        val table = arrayListOf<List<FieldViewModel>>()
        repeat(2){ row ->
            val fields = arrayListOf<FieldViewModel>()
            repeat(2){ column ->
                fields.add(
                    EditTextViewModel.create(
                        "",
                        "",
                        false,
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
