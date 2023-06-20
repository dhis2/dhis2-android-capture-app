package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.SortedMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableModel
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DataValuePresenterTest {

    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var presenter: DataValuePresenter

    private val view: DataValueContract.View = mock()
    private val dataValueRepository: DataValueRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val valueStore: ValueStore = mock()
    private val tableDimensionStore: TableDimensionStore = mock()
    private val testingDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider: DispatcherProvider = mock {
        on { io() } doReturn testingDispatcher
        on { ui() } doReturn testingDispatcher
    }
    private val mapper: TableDataToTableModelMapper = mock()
    private val updateCellValueMocked: (TableCell) -> Unit = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testingDispatcher)
        whenever(dataValueRepository.getDataSetInfo()) doReturn Triple(
            "periodId",
            "orgUnitId",
            "attrOptCombo"
        )
        presenter =
            DataValuePresenter(
                view,
                dataValueRepository,
                valueStore,
                tableDimensionStore,
                schedulers,
                mapper,
                dispatcherProvider
            )
    }

    @Test
    fun shouldInitData() {
        whenever(dataValueRepository.getCatCombo()) doReturn Flowable.just(mockedCatCombos)
        whenever(
            dataValueRepository.getDataTableModel(any<CategoryCombo>())
        ) doReturn Observable.just(
            mockedDataTableModel
        )
        whenever(dataValueRepository.setTableData(any(), any())) doReturn mockedTableData
        whenever(mapper.invoke(any())) doReturn mockedTableModel

        whenever(dataValueRepository.getDataSetIndicators()) doReturn Single.just(mockedIndicators)
        whenever(mapper.map(any())) doReturn mockedTableModel

        presenter.init()

        val tableStateValue = presenter.currentState().value
        assertTrue(tableStateValue.tables.isNotEmpty())
        assertTrue(tableStateValue.tables.size == 2)
    }

    @Test
    fun shouldShowOptionSetDialog() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
        }
        val mockedDataElement = mock<DataElement> {
            on { optionSetUid() } doReturn "optionSetUid"
        }
        val mockedSpinnerModel = mock<SpinnerViewModel>()
        whenever(dataValueRepository.getDataElement(any())) doReturn mockedDataElement
        whenever(
            dataValueRepository.getOptionSetViewModel(any(), any())
        ) doReturn mockedSpinnerModel

        presenter.onCellClick("tableId", mockedTableCell, updateCellValueMocked)

        verify(view).showOptionSetDialog(
            mockedDataElement,
            mockedTableCell,
            mockedSpinnerModel,
            updateCellValueMocked
        )
    }

    @Test
    fun shouldShowBooleanDialog() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
        }

        val mockedDataElement = mock<DataElement> {
            on { optionSetUid() } doReturn null
            on { valueType() } doReturn ValueType.BOOLEAN
        }

        whenever(dataValueRepository.getDataElement(any())) doReturn mockedDataElement

        presenter.onCellClick("tableId", mockedTableCell, updateCellValueMocked)
        verify(view).showBooleanDialog(mockedDataElement, mockedTableCell, updateCellValueMocked)
    }

    @Test
    fun shouldShowBooleanDialogForTrueOnlyValueType() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
        }

        val mockedDataElement = mock<DataElement> {
            on { optionSetUid() } doReturn null
            on { valueType() } doReturn ValueType.TRUE_ONLY
        }

        whenever(dataValueRepository.getDataElement(any())) doReturn mockedDataElement

        presenter.onCellClick("tableId", mockedTableCell, updateCellValueMocked)
        verify(view).showBooleanDialog(mockedDataElement, mockedTableCell, updateCellValueMocked)
    }

    @Test
    fun shouldShowCalendar() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
        }

        val mockedDataElement = mock<DataElement> {
            on { optionSetUid() } doReturn null
            on { valueType() } doReturn ValueType.DATE
        }

        whenever(dataValueRepository.getDataElement(any())) doReturn mockedDataElement

        presenter.onCellClick("tableId", mockedTableCell, updateCellValueMocked)
        verify(view).showCalendar(mockedDataElement, mockedTableCell, false, updateCellValueMocked)
    }

    @Test
    fun shouldShowCalendarWithTime() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
        }

        val mockedDataElement = mock<DataElement> {
            on { optionSetUid() } doReturn null
            on { valueType() } doReturn ValueType.DATETIME
        }

        whenever(dataValueRepository.getDataElement(any())) doReturn mockedDataElement

        presenter.onCellClick("tableId", mockedTableCell, updateCellValueMocked)
        verify(view).showCalendar(mockedDataElement, mockedTableCell, true, updateCellValueMocked)
    }

    @Test
    fun shouldShowTimePicker() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
        }

        val mockedDataElement = mock<DataElement> {
            on { optionSetUid() } doReturn null
            on { valueType() } doReturn ValueType.TIME
        }

        whenever(dataValueRepository.getDataElement(any())) doReturn mockedDataElement

        presenter.onCellClick("tableId", mockedTableCell, updateCellValueMocked)
        verify(view).showTimePicker(mockedDataElement, mockedTableCell, updateCellValueMocked)
    }

    @Test
    fun shouldShowCoordinateDialog() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
        }

        val mockedDataElement = mock<DataElement> {
            on { optionSetUid() } doReturn null
            on { valueType() } doReturn ValueType.COORDINATE
        }

        whenever(dataValueRepository.getDataElement(any())) doReturn mockedDataElement

        presenter.onCellClick("tableId", mockedTableCell, updateCellValueMocked)
        verify(view).showCoordinatesDialog(
            mockedDataElement,
            mockedTableCell,
            updateCellValueMocked
        )
    }

    @Test
    fun shouldShowOrgUnitDialog() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
        }

        val mockedDataElement = mock<DataElement> {
            on { optionSetUid() } doReturn null
            on { valueType() } doReturn ValueType.ORGANISATION_UNIT
        }

        val mockedOrgUnits = listOf<OrganisationUnit>()
        whenever(dataValueRepository.getDataElement(any())) doReturn mockedDataElement
        whenever(dataValueRepository.orgUnits()) doReturn mockedOrgUnits
        presenter.onCellClick("tableId", mockedTableCell, updateCellValueMocked)
        verify(view).showOtgUnitDialog(
            mockedDataElement,
            mockedTableCell,
            mockedOrgUnits,
            updateCellValueMocked
        )
    }

    @Test
    fun shouldShowAgeDialog() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
        }

        val mockedDataElement = mock<DataElement> {
            on { optionSetUid() } doReturn null
            on { valueType() } doReturn ValueType.AGE
        }

        whenever(dataValueRepository.getDataElement(any())) doReturn mockedDataElement

        presenter.onCellClick("tableId", mockedTableCell, updateCellValueMocked)
        verify(view).showAgeDialog(mockedDataElement, mockedTableCell, updateCellValueMocked)
    }

    @Test
    fun shouldReturnTextInputModel() {
        val inputValueTypes = listOf(
            ValueType.TEXT,
            ValueType.LONG_TEXT,
            ValueType.LETTER,
            ValueType.PHONE_NUMBER,
            ValueType.EMAIL,
            ValueType.NUMBER,
            ValueType.UNIT_INTERVAL,
            ValueType.PERCENTAGE,
            ValueType.INTEGER,
            ValueType.INTEGER_NEGATIVE,
            ValueType.INTEGER_POSITIVE,
            ValueType.INTEGER_ZERO_OR_POSITIVE,
            ValueType.URL
        )

        inputValueTypes.forEach { valueType ->
            val mockedTableCell = mock<TableCell> {
                on { id } doReturn "mocked_id"
                on { column } doReturn 1
                on { value } doReturn "celLValue"
            }

            val mockedDataElement = mock<DataElement> {
                on { optionSetUid() } doReturn null
                on { displayFormName() } doReturn valueType.name
                on { valueType() } doReturn valueType
            }

            whenever(dataValueRepository.getDataElement(any())) doReturn mockedDataElement
            whenever(
                dataValueRepository.getCatOptComboOptions(any())
            ) doReturn listOf("option_${valueType.name}")
            val textInput = presenter.onCellClick(
                "tableId",
                mockedTableCell,
                updateCellValueMocked
            )
            assertTrue(textInput != null)
        }
    }

    @Test
    fun shouldSaveValue() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
            on { row } doReturn 0
            on { value } doReturn "valueToSave"
        }

        val mockedTableModel = mock<TableModel> {
            on { id } doReturn "tableId"
            on { hasCellWithId(any()) } doReturn true
            on { overwrittenValues } doReturn emptyMap()
        }

        val mockedUpdatedTableModel = mock<TableModel> {
            on { id } doReturn "tableId_updated"
            on { hasCellWithId(any()) } doReturn true
        }

        val tableStateValue = presenter.mutableTableData()
        tableStateValue.value = TableScreenState(listOf(mockedTableModel))

        whenever(valueStore.save(any(), any(), any(), any(), any(), any())) doReturn Flowable.just(
            StoreResult(
                uid = "id",
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
                valueStoreResultMessage = null
            )
        )

        whenever(dataValueRepository.getDataTableModel(any<String>())) doReturn Observable.just(
            mockedDataTableModel
        )
        whenever(dataValueRepository.setTableData(any(), any())) doReturn mockedTableData
        whenever(mapper.invoke(any())) doReturn mockedUpdatedTableModel
        whenever(
            mockedUpdatedTableModel.copy(overwrittenValues = mockedTableModel.overwrittenValues)
        ) doReturn mockedUpdatedTableModel

        presenter.onSaveValueChange(mockedTableCell)

        assertTrue(presenter.mutableTableData().value.tables.size == 1)
        assertTrue(presenter.mutableTableData().value.tables[0].id == "tableId_updated")
        verify(view).onValueProcessed()
    }

    @Test
    fun shouldUpdateIndicatorValueWhenSaved() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
            on { row } doReturn 0
            on { value } doReturn "valueToSave"
        }

        val mockedTableModel = mock<TableModel> {
            on { id } doReturn "tableId"
            on { hasCellWithId(any()) } doReturn true
        }

        val mockedIndicatorTableModel = mock<TableModel> {
            on { id } doReturn null
        }

        val mockedUpdatedTableModel = mock<TableModel> {
            on { id } doReturn "tableId_updated"
            on { hasCellWithId(any()) } doReturn true
        }

        val mockedUpdatedIndicatorTableModel = mock<TableModel> {
            on { id } doReturn "updated_indicator"
        }

        val tableStateValue = presenter.mutableTableData()
        tableStateValue.value = TableScreenState(
            listOf(mockedTableModel, mockedIndicatorTableModel)
        )

        whenever(valueStore.save(any(), any(), any(), any(), any(), any())) doReturn Flowable.just(
            StoreResult(
                uid = "id",
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
                valueStoreResultMessage = null
            )
        )

        whenever(dataValueRepository.getDataTableModel(any<String>())) doReturn Observable.just(
            mockedDataTableModel
        )
        whenever(dataValueRepository.setTableData(any(), any())) doReturn mockedTableData
        whenever(mapper.invoke(any())) doReturn mockedUpdatedTableModel
        whenever(
            mockedUpdatedTableModel.copy(overwrittenValues = mockedTableModel.overwrittenValues)
        ) doReturn mockedUpdatedTableModel

        whenever(dataValueRepository.getDataSetIndicators()) doReturn Single.just(mockedIndicators)
        whenever(mapper.map(any())) doReturn mockedUpdatedIndicatorTableModel

        presenter.onSaveValueChange(mockedTableCell)

        assertTrue(presenter.currentState().value.tables.size == 2)
        assertTrue(presenter.currentState().value.tables[0].id == "tableId_updated")
        assertTrue(presenter.currentState().value.tables.last().id == "updated_indicator")
        verify(view).onValueProcessed()
    }

    @Test
    fun shouldSetErrorValue() {
        val mockedTableCell = mock<TableCell> {
            on { id } doReturn "mocked_id"
            on { column } doReturn 1
            on { row } doReturn 0
            on { value } doReturn "valueToSave"
        }

        val mockedTableModel = mock<TableModel> {
            on { id } doReturn "tableId"
            on { hasCellWithId(any()) } doReturn true
            on { overwrittenValues } doReturn emptyMap()
        }

        val mockedUpdatedTableModel = mock<TableModel> {
            on { id } doReturn "tableId_updated"
            on { hasCellWithId(any()) } doReturn true
        }

        val tableStateValue = presenter.mutableTableData()
        tableStateValue.value = TableScreenState(listOf(mockedTableModel))

        whenever(valueStore.save(any(), any(), any(), any(), any(), any())) doReturn Flowable.just(
            StoreResult(
                uid = "id",
                valueStoreResult = ValueStoreResult.ERROR_UPDATING_VALUE,
                valueStoreResultMessage = "This is an error"
            )
        )

        whenever(dataValueRepository.getDataTableModel(any<String>())) doReturn Observable.just(
            mockedDataTableModel
        )
        whenever(dataValueRepository.setTableData(any(), any())) doReturn mockedTableData
        whenever(mapper.invoke(any())) doReturn mockedUpdatedTableModel
        whenever(
            mockedUpdatedTableModel.copy(overwrittenValues = mockedTableModel.overwrittenValues)
        ) doReturn mockedUpdatedTableModel

        presenter.onSaveValueChange(mockedTableCell)

        assertTrue(presenter.currentState().value.tables.size == 1)
        assertTrue(presenter.currentState().value.tables[0].id == "tableId_updated")
        assertTrue(presenter.errors().isNotEmpty())
        verify(view).onValueProcessed()
    }

    private val mockedCatCombos = listOf<CategoryCombo>(mock())
    private val mockedDataTableModel = mock<DataTableModel>()
    private val mockedTableData = mock<TableData>()
    private val mockedTableModel = mock<TableModel> { }
    private val mockedIndicators = mock<SortedMap<String?, String>>()
}
