package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import org.dhis2.Bindings.toDate
import org.dhis2.R
import org.dhis2.commons.Constants.ACCESS_DATA
import org.dhis2.commons.Constants.DATA_SET_SECTION
import org.dhis2.commons.Constants.DATA_SET_UID
import org.dhis2.commons.dialogs.DialogClickListener
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.composetable.actions.TableResizeActions
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.ui.DataSetTableScreen
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableConfiguration
import org.dhis2.composetable.ui.TableDimensions
import org.dhis2.composetable.ui.TableTheme
import org.dhis2.composetable.ui.semantics.MAX_CELL_WIDTH_SPACE
import org.dhis2.data.forms.dataentry.tablefields.age.AgeView
import org.dhis2.data.forms.dataentry.tablefields.coordinate.CoordinatesView
import org.dhis2.data.forms.dataentry.tablefields.radiobutton.YesNoView
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.dataSetTable.DataSetTablePresenter
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.DateUtils
import org.dhis2.utils.customviews.OptionSetOnClickListener
import org.dhis2.utils.customviews.TableFieldDialog
import org.dhis2.utils.optionset.OptionSetDialog
import org.dhis2.utils.optionset.OptionSetDialog.Companion.TAG
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

const val ARG_ORG_UNIT = "ARG_ORG_UNIT"
const val ARG_PERIOD_ID = "ARG_PERIOD_ID"
const val ARG_ATTR_OPT_COMB = "ARG_ATTR_OPT_COMB"

class DataSetSectionFragment : FragmentGlobalAbstract(), DataValueContract.View {

    private lateinit var activity: DataSetTableActivity
    private lateinit var presenter: DataSetTablePresenter

    @Inject
    lateinit var presenterFragment: DataValuePresenter

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        abstractActivity.hideKeyboard()
        requireView().clearFocus()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as DataSetTableActivity
        presenter = activity.presenter

        activity.dataSetTableComponent?.plus(
            DataValueModule(
                arguments?.getString(DATA_SET_UID)!!,
                arguments?.getString(DATA_SET_SECTION)!!,
                arguments?.getString(ARG_ORG_UNIT)!!,
                arguments?.getString(ARG_PERIOD_ID)!!,
                arguments?.getString(ARG_ATTR_OPT_COMB)!!,
                this,
                activity
            )
        )?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MdcTheme {
                    val localDensity = LocalDensity.current
                    val conf = LocalConfiguration.current
                    val tableConfState by presenterFragment.currentTableConfState().collectAsState()

                    var dimensions by remember {
                        mutableStateOf(
                            TableDimensions(
                                cellVerticalPadding = 11.dp,
                                maxRowHeaderWidth = with(localDensity) {
                                    (conf.screenWidthDp.dp.toPx() - MAX_CELL_WIDTH_SPACE.toPx())
                                        .roundToInt()
                                },
                                extraWidths = with(localDensity) {
                                    tableConfState.overwrittenTableWidth?.mapValues { (_, width) ->
                                        width.dp.roundToPx()
                                    }
                                } ?: emptyMap(),
                                rowHeaderWidths = with(localDensity) {
                                    tableConfState.overwrittenRowHeaderWidth
                                        ?.mapValues { (_, width) ->
                                            width.dp.roundToPx()
                                        }
                                } ?: emptyMap(),
                                columnWidth = with(localDensity) {
                                    tableConfState.overwrittenColumnWidth?.mapValues { (_, value) ->
                                        value.mapValues { (_, width) ->
                                            width.dp.roundToPx()
                                        }
                                    }
                                } ?: emptyMap()
                            )
                        )
                    }

                    val tableResizeActions = object : TableResizeActions {
                        override fun onTableWidthChanged(width: Int) {
                            val tableEndExtraScroll = with(localDensity) {
                                dimensions.tableEndExtraScroll.toPx().toInt()
                            }
                            dimensions = dimensions.copy(
                                totalWidth = width - tableEndExtraScroll
                            )
                        }

                        override fun onRowHeaderResize(tableId: String, newValue: Float) {
                            with(localDensity) {
                                dimensions = dimensions.updateHeaderWidth(tableId, newValue)
                                val widthDpValue =
                                    dimensions.getRowHeaderWidth(tableId).toDp().value
                                presenterFragment.saveWidth(tableId, widthDpValue)
                            }
                        }

                        override fun onColumnHeaderResize(
                            tableId: String,
                            column: Int,
                            newValue: Float
                        ) {
                            with(localDensity) {
                                dimensions =
                                    dimensions.updateColumnWidth(tableId, column, newValue)
                                val widthDpValue =
                                    dimensions.getColumnWidth(tableId, column).toDp().value
                                presenterFragment.saveColumnWidth(tableId, column, widthDpValue)
                            }
                        }

                        override fun onTableDimensionResize(tableId: String, newValue: Float) {
                            with(localDensity) {
                                dimensions = dimensions.updateAllWidthBy(tableId, newValue)
                                val widthDpValue =
                                    dimensions.getExtraWidths(tableId).toDp().value
                                presenterFragment.saveTableWidth(tableId, widthDpValue)
                            }
                        }

                        override fun onTableDimensionReset(tableId: String) {
                            dimensions = dimensions.resetWidth(tableId)
                            presenterFragment.resetTableDimensions(tableId)
                        }
                    }

                    TableTheme(
                        tableColors = TableColors(
                            primary = MaterialTheme.colors.primary,
                            primaryLight = MaterialTheme.colors.primary.copy(alpha = 0.2f),
                            disabledSelectedBackground = MaterialTheme.colors.primary.copy(
                                alpha = 0.5f
                            )
                        ),
                        tableDimensions = dimensions,
                        tableConfiguration = TableConfiguration(),
                        tableValidator = presenterFragment,
                        tableResizeActions = tableResizeActions
                    ) {
                        val screenState by presenterFragment.currentState().collectAsState()

                        DataSetTableScreen(
                            tableScreenState = screenState,
                            onCellClick = presenterFragment::onCellClick,
                            onEdition = presenter::editingCellValue,
                            onSaveValue = presenterFragment::onSaveValueChange
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenterFragment.init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenterFragment.onDettach()
    }

    override fun onValueProcessed() {
        if (activity.isBackPressed) {
            activity.abstractActivity.back()
        }
    }

    override fun showCalendar(
        dataElement: DataElement,
        cell: TableCell,
        showTimePicker: Boolean,
        updateCellValue: (TableCell) -> Unit
    ) {
        val dialog = CalendarPicker(requireContext())
        dialog.setTitle(dataElement.displayFormName())

        val calendar = Calendar.getInstance()
        if (!cell.value.isNullOrEmpty()) {
            dialog.setInitialDate(cell.value!!.toDate())
            calendar.time = cell.value!!.toDate()
        }
        dialog.isFutureDatesAllowed(true)
        dialog.setListener(object : OnDatePickerListener {
            override fun onNegativeClick() {
                val updatedCellValue = cell.copy(value = null)
                updateCellValue(updatedCellValue)
                presenterFragment.onSaveValueChange(updatedCellValue)
            }

            override fun onPositiveClick(datePicker: DatePicker) {
                calendar[Calendar.YEAR] = datePicker.year
                calendar[Calendar.MONTH] = datePicker.month
                calendar[Calendar.DAY_OF_MONTH] = datePicker.dayOfMonth
                if (showTimePicker) {
                    showDateTime(dataElement, cell, calendar, updateCellValue)
                } else {
                    calendar[Calendar.HOUR_OF_DAY] = 0
                    calendar[Calendar.MINUTE] = 0
                    val selectedDate: Date = calendar.time
                    val result = DateUtils.oldUiDateFormat().format(selectedDate)
                    val updatedCellValue = cell.copy(value = result)
                    updateCellValue(updatedCellValue)
                    presenterFragment.onSaveValueChange(updatedCellValue)
                }
            }
        })
        dialog.show()
    }

    private fun showDateTime(
        dataElement: DataElement,
        cell: TableCell,
        calendar: Calendar,
        updateCellValue: (TableCell) -> Unit
    ) {
        val is24HourFormat = DateFormat.is24HourFormat(context)
        MaterialTimePicker.Builder()
            .setTheme(org.dhis2.form.R.style.TimePicker)
            .setTimeFormat(TimeFormat.CLOCK_24H.takeIf { is24HourFormat } ?: TimeFormat.CLOCK_12H)
            .setHour(calendar[Calendar.HOUR_OF_DAY])
            .setMinute(calendar[Calendar.MINUTE])
            .setTitleText(dataElement.displayFormName())
            .build().apply {
                addOnPositiveButtonClickListener {
                    calendar[Calendar.HOUR_OF_DAY] = hour
                    calendar[Calendar.MINUTE] = minute
                    val result = DateUtils.databaseDateFormatNoSeconds().format(calendar.time)
                    val updatedCellValue = cell.copy(value = result)
                    updateCellValue(updatedCellValue)
                    presenterFragment.onSaveValueChange(updatedCellValue)
                }
            }
            .show(childFragmentManager, "timePicker")
    }

    override fun showTimePicker(
        dataElement: DataElement,
        cell: TableCell,
        updateCellValue: (TableCell) -> Unit
    ) {
        val c = Calendar.getInstance()
        if (!cell.value.isNullOrEmpty()) {
            c.time = DateUtils.timeFormat().parse(cell.value!!)!!
        }

        val twentyFourHourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val twelveHourFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val is24HourFormat = DateFormat.is24HourFormat(context)
        MaterialTimePicker.Builder()
            .setTheme(org.dhis2.form.R.style.TimePicker)
            .setTimeFormat(TimeFormat.CLOCK_24H.takeIf { is24HourFormat } ?: TimeFormat.CLOCK_12H)
            .setHour(c[Calendar.HOUR_OF_DAY])
            .setMinute(c[Calendar.MINUTE])
            .setTitleText(dataElement.displayFormName())
            .setNegativeButtonText(R.string.date_dialog_clear)
            .build().apply {
                addOnPositiveButtonClickListener {
                    val calendar = Calendar.getInstance()
                    calendar[Calendar.HOUR_OF_DAY] = hour
                    calendar[Calendar.MINUTE] = minute
                    val selectedDate = calendar.time
                    val calendarTime: String = if (is24HourFormat) {
                        twentyFourHourFormat.format(selectedDate)
                    } else {
                        twelveHourFormat.format(selectedDate)
                    }
                    val updatedCellValue = cell.copy(value = calendarTime)
                    updateCellValue(updatedCellValue)
                    presenterFragment.onSaveValueChange(updatedCellValue)
                }
                addOnNegativeButtonClickListener {
                    val updatedCellValue = cell.copy(value = null)
                    updateCellValue(updatedCellValue)
                    presenterFragment.onSaveValueChange(updatedCellValue)
                }
            }
            .show(childFragmentManager, "timePicker")
    }

    override fun showBooleanDialog(
        dataElement: DataElement,
        cell: TableCell,
        updateCellValue: (TableCell) -> Unit
    ) {
        val yesNoView = YesNoView(context)
        yesNoView.setIsBgTransparent(true)
        yesNoView.setValueType(dataElement.valueType())
        yesNoView.setRendering(ValueTypeRenderingType.DEFAULT)
        yesNoView.clearButton.visibility = View.GONE

        if (!cell.value.isNullOrEmpty()) {
            if (cell.value.toBoolean()) {
                yesNoView.radioGroup.check(R.id.yes)
            } else {
                yesNoView.radioGroup.check(R.id.no)
            }
        }

        TableFieldDialog(
            requireContext(),
            dataElement.displayFormName()!!,
            dataElement.displayDescription() ?: "",
            yesNoView,
            object : DialogClickListener {
                override fun onPositive() {
                    val newValue = when (yesNoView.radioGroup.checkedRadioButtonId) {
                        R.id.yes -> true.toString()
                        R.id.no -> false.toString()
                        else -> null
                    }
                    val updatedCellValue = cell.copy(value = newValue)
                    updateCellValue(updatedCellValue)
                    presenterFragment.onSaveValueChange(updatedCellValue)
                }

                override fun onNegative() {}
            }
        ) {
            yesNoView.radioGroup.clearCheck()
        }.show()
    }

    override fun showAgeDialog(
        dataElement: DataElement,
        cell: TableCell,
        updateCellValue: (TableCell) -> Unit
    ) {
        val ageView = AgeView(context)
        ageView.setIsBgTransparent()
        if (!cell.value.isNullOrEmpty()) {
            ageView.setInitialValue(cell.value)
        }

        TableFieldDialog(
            requireContext(),
            dataElement.displayFormName()!!,
            dataElement.displayDescription() ?: "",
            ageView,
            object : DialogClickListener {

                override fun onPositive() {
                    val date: String = ageView.selectedDate?.let {
                        DateUtils.oldUiDateFormat().format(it)
                    } ?: ""
                    if (cell.value != date) {
                        val updatedCellValue = cell.copy(value = date)
                        presenterFragment.onSaveValueChange(updatedCellValue)
                        updateCellValue(cell.copy(value = date))
                    }
                }

                override fun onNegative() {}
            },
            null
        ).show()
    }

    override fun showCoordinatesDialog(
        dataElement: DataElement,
        cell: TableCell,
        updateCellValue: (TableCell) -> Unit
    ) {
        val coordinatesView = CoordinatesView(context)
        coordinatesView.setIsBgTransparent(true)
        coordinatesView.featureType = FeatureType.POINT
        if (!cell.value.isNullOrEmpty()) {
            coordinatesView.setInitialValue(cell.value)
        }

        TableFieldDialog(
            requireContext(),
            dataElement.displayFormName()!!,
            dataElement.displayDescription() ?: "",
            coordinatesView,
            object : DialogClickListener {
                override fun onPositive() {
                    if (cell.value != coordinatesView.currentCoordinates()) {
                        val updatedCellValue =
                            cell.copy(value = coordinatesView.currentCoordinates())
                        updateCellValue(updatedCellValue)
                        presenterFragment.onSaveValueChange(updatedCellValue)
                    }
                }

                override fun onNegative() {}
            },
            null
        ).show()
    }

    override fun showOtgUnitDialog(
        dataElement: DataElement,
        cell: TableCell,
        orgUnits: List<OrganisationUnit>,
        updateCellValue: (TableCell) -> Unit
    ) {
        OUTreeFragment.Builder()
            .showAsDialog()
            .singleSelection()
            .withPreselectedOrgUnits(cell.value?.let { listOf(it) } ?: emptyList())
            .onSelection { selectedOrgUnits ->
                val updatedCellValue = cell.copy(value = selectedOrgUnits[0].uid())
                updateCellValue(updatedCellValue)
                presenterFragment.onSaveValueChange(updatedCellValue)
            }
            .build()
            .show(childFragmentManager, dataElement.displayFormName())
    }

    override fun showOptionSetDialog(
        dataElement: DataElement,
        cell: TableCell,
        spinnerViewModel: SpinnerViewModel,
        updateCellValue: (TableCell) -> Unit
    ) {
        val dialog = OptionSetDialog()
        dialog.create(requireContext())
        dialog.optionSetTable = spinnerViewModel

        /**
         * This code is commented because I can't find a way to anchor PopUpMenu to compose item
         * I have created a issue() to refactor OptionSetCellPopUp to compose in order to use it in both sides
         * After implement new menu, please, uncomment this code
         */
        if (dialog.showDialog()) {
            dialog.listener = OptionSetOnClickListener {
                val updatedCellValue = cell.copy(value = it.code())
                updateCellValue(updatedCellValue)
                presenterFragment.onSaveValueChange(updatedCellValue)
            }
            dialog.clearListener = View.OnClickListener {
                val updatedCellValue = cell.copy(value = null)
                updateCellValue(updatedCellValue)
                presenterFragment.onSaveValueChange(updatedCellValue)
            }
            dialog.show(parentFragmentManager, TAG)
        } else {
            dialog.dismiss()
            presenterFragment.onSaveValueChange(cell)
        }
    }

    companion object {

        @JvmStatic
        fun create(
            sectionUid: String,
            accessDataWrite: Boolean,
            dataSetUid: String,
            orgUnitUid: String,
            periodId: String,
            attributeOptionComboUid: String
        ): DataSetSectionFragment {
            val bundle = Bundle()
            bundle.putString(DATA_SET_SECTION, sectionUid)
            bundle.putBoolean(ACCESS_DATA, accessDataWrite)
            bundle.putString(DATA_SET_UID, dataSetUid)
            bundle.putString(ARG_ORG_UNIT, orgUnitUid)
            bundle.putString(ARG_PERIOD_ID, periodId)
            bundle.putString(ARG_ATTR_OPT_COMB, attributeOptionComboUid)
            val dataSetSectionFragment = DataSetSectionFragment()
            dataSetSectionFragment.arguments = bundle
            return dataSetSectionFragment
        }
    }
}
