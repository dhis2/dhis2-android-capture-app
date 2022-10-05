package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.composethemeadapter.MdcTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import org.dhis2.Bindings.toDate
import org.dhis2.R
import org.dhis2.commons.Constants.ACCESS_DATA
import org.dhis2.commons.Constants.DATA_SET_SECTION
import org.dhis2.commons.Constants.DATA_SET_UID
import org.dhis2.commons.dialogs.DialogClickListener
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.ui.DataSetTableScreen
import org.dhis2.data.forms.dataentry.tablefields.age.AgeView
import org.dhis2.data.forms.dataentry.tablefields.coordinate.CoordinatesView
import org.dhis2.data.forms.dataentry.tablefields.radiobutton.YesNoView
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.DateUtils
import org.dhis2.utils.customviews.OptionSetOnClickListener
import org.dhis2.utils.customviews.OrgUnitDialog
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
    private lateinit var presenter: DataSetTableContract.Presenter

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

        activity.dataSetTableComponent.plus(
            DataValueModule(
                arguments?.getString(DATA_SET_UID)!!,
                arguments?.getString(DATA_SET_SECTION)!!,
                arguments?.getString(ARG_ORG_UNIT)!!,
                arguments?.getString(ARG_PERIOD_ID)!!,
                arguments?.getString(ARG_ATTR_OPT_COMB)!!,
                this,
                activity
            )
        ).inject(this)
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
                    val tableData by presenterFragment.tableData()
                        .observeAsState(emptyList())

                    DataSetTableScreen(
                        tableData = tableData,
                        onCellClick = { _, cell ->
                            presenterFragment.onCellClick(cell = cell)
                        },
                        onEdition = { isEditing ->
                            presenter.editingCellValue(isEditing)
                        },
                        onCellValueChange = { cell ->
                            presenterFragment.onCellValueChanged(cell)
                        },
                        onSaveValue = { cell ->
                            presenterFragment.onSaveValueChange(cell)
                        }
                    )
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

    override fun update(modified: Boolean) {
        if (modified) {
            activity.update()
        }
    }

    override fun showCalendar(dataElement: DataElement, cell: TableCell, showTimePicker: Boolean) {
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
                presenterFragment.onSaveValueChange(cell.copy(value = null))
            }

            override fun onPositiveClick(datePicker: DatePicker) {
                calendar.set(Calendar.YEAR, datePicker.year)
                calendar.set(Calendar.MONTH, datePicker.month)
                calendar.set(Calendar.DAY_OF_MONTH, datePicker.dayOfMonth)
                if (showTimePicker) {
                    showDateTime(dataElement, cell, calendar)
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    val selectedDate: Date = calendar.time
                    val result = DateUtils.oldUiDateFormat().format(selectedDate)
                    presenterFragment.onSaveValueChange(cell.copy(value = result))
                }
            }
        })
        dialog.show()
    }

    private fun showDateTime(dataElement: DataElement, cell: TableCell, calendar: Calendar) {
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val is24HourFormat = DateFormat.is24HourFormat(context)

        val dialog = TimePickerDialog(
            context,
            { _: TimePicker?, hourOfDay: Int, minutes: Int ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minutes)
                val result = DateUtils.databaseDateFormatNoSeconds().format(calendar.time)
                presenterFragment.onSaveValueChange(cell.copy(value = result))
            },
            hour,
            minute,
            is24HourFormat
        )
        dialog.setTitle(dataElement.displayFormName())
        dialog.show()
    }

    override fun showTimePicker(dataElement: DataElement, cell: TableCell) {
        val c = Calendar.getInstance()
        if (!cell.value.isNullOrEmpty()) {
            c.time = DateUtils.timeFormat().parse(cell.value!!)!!
        }

        val hour = c[Calendar.HOUR_OF_DAY]
        val minute = c[Calendar.MINUTE]
        val is24HourFormat = DateFormat.is24HourFormat(context)
        val twentyFourHourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val twelveHourFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dialog = TimePickerDialog(
            context,
            { _: TimePicker?, hourOfDay: Int, minutes: Int ->
                val calendar = Calendar.getInstance()
                calendar[Calendar.HOUR_OF_DAY] = hourOfDay
                calendar[Calendar.MINUTE] = minutes
                val selectedDate = calendar.time
                val calendarTime: String = if (is24HourFormat) {
                    twentyFourHourFormat.format(selectedDate)
                } else {
                    twelveHourFormat.format(selectedDate)
                }
                presenterFragment.onSaveValueChange(cell.copy(value = calendarTime))
            },
            hour,
            minute,
            is24HourFormat
        )
        dialog.setTitle(dataElement.displayFormName())

        dialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            requireContext().getString(R.string.date_dialog_clear)
        ) { _: DialogInterface?, _: Int ->
            presenterFragment.onSaveValueChange(cell.copy(value = null))
        }
        dialog.show()
    }

    override fun showBooleanDialog(dataElement: DataElement, cell: TableCell) {
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
                    presenterFragment.onSaveValueChange(cell.copy(value = newValue))
                }

                override fun onNegative() {}
            }
        ) {
            yesNoView.radioGroup.clearCheck()
        }.show()
    }

    override fun showAgeDialog(dataElement: DataElement, cell: TableCell) {
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
                        presenterFragment.onSaveValueChange(cell.copy(value = date))
                    }
                }

                override fun onNegative() {}
            },
            null
        ).show()
    }

    override fun showCoordinatesDialog(dataElement: DataElement, cell: TableCell) {
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
                        presenterFragment.onSaveValueChange(
                            cell.copy(value = coordinatesView.currentCoordinates())
                        )
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
        orgUnits: List<OrganisationUnit>
    ) {
        val orgUnitDialog = OrgUnitDialog()
        orgUnitDialog.setTitle(dataElement.displayFormName())
            .setMultiSelection(false)
            .setOrgUnits(orgUnits)
            .setPossitiveListener {
                presenterFragment.onSaveValueChange(
                    cell.copy(value = orgUnitDialog.selectedOrgUnit)
                )
                orgUnitDialog.dismiss()
            }
            .setNegativeListener {
                orgUnitDialog.dismiss()
            }
        if (!orgUnitDialog.isAdded) {
            orgUnitDialog.show(
                parentFragmentManager,
                dataElement.displayFormName()
            )
        }
    }

    override fun showOptionSetDialog(
        dataElement: DataElement,
        cell: TableCell,
        spinnerViewModel: SpinnerViewModel
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
                presenterFragment.onSaveValueChange(cell.copy(value = it.code()))
            }
            dialog.clearListener = View.OnClickListener {
                presenterFragment.onSaveValueChange(cell.copy(value = null))
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
