package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.text.format.DateFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TimePicker
import android.widget.Toast
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.MutableLiveData
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.adapter.recyclerview.CellRecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.SortedMap
import javax.inject.Inject
import org.dhis2.Bindings.calculateWidth
import org.dhis2.Bindings.dp
import org.dhis2.Bindings.measureText
import org.dhis2.Bindings.toDate
import org.dhis2.R
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableList
import org.dhis2.commons.dialogs.DialogClickListener
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.ui.TableList
import org.dhis2.data.forms.dataentry.tablefields.RowAction
import org.dhis2.data.forms.dataentry.tablefields.age.AgeView
import org.dhis2.data.forms.dataentry.tablefields.coordinate.CoordinatesView
import org.dhis2.data.forms.dataentry.tablefields.radiobutton.YesNoView
import org.dhis2.databinding.FragmentDatasetSectionBinding
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.Constants.ACCESS_DATA
import org.dhis2.utils.Constants.DATA_SET_SECTION
import org.dhis2.utils.Constants.DATA_SET_UID
import org.dhis2.utils.DateUtils
import org.dhis2.utils.customviews.TableFieldDialog
import org.dhis2.utils.isPortrait
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.hisp.dhis.android.core.dataelement.DataElement

const val ARG_ORG_UNIT = "ARG_ORG_UNIT"
const val ARG_PERIOD_ID = "ARG_PERIOD_ID"
const val ARG_ATTR_OPT_COMB = "ARG_ATTR_OPT_COMB"

class DataSetSectionFragment : FragmentGlobalAbstract(), DataValueContract.View {

    private lateinit var binding: FragmentDatasetSectionBinding
    private lateinit var activity: DataSetTableActivity
    private lateinit var presenter: DataSetTableContract.Presenter

    private val adapters = ArrayList<DataSetTableAdapter>()

    @Inject
    lateinit var presenterFragment: DataValuePresenter

    private var heights = ArrayList<Int>()
    private val currentTablePosition = MutableLiveData<Int>()
    private var tablesCount: Int = 0
    private var indicatorsTable: TableView? = null
    private val saveToast: Toast by lazy {
        Toast.makeText(requireContext(), R.string.datavalue_saved, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.TOP or Gravity.START, 16.dp, 110.dp)
        }
    }

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
                this
            )
        ).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentDatasetSectionBinding.inflate(inflater, container, false)
            .also {
                binding = it
                if (presenterFragment.isComposeTableEnable()) {
                    binding.tables.setContent {
                        MdcTheme {
                            val tableData by presenterFragment.tableData()
                                .observeAsState(emptyList())
                            TableList(
                                tableList = tableData,
                                tableColors = TableColors(
                                    primary = MaterialTheme.colors.primary,
                                    primaryLight = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                                )
                            ) { cell -> presenterFragment.onCellClick(cell) }
                        }
                    }
                }
            }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentTablePosition.observe(viewLifecycleOwner, { loadHeader(it) })
        presenterFragment.init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenterFragment.onDettach()
    }

    override fun setTableData(tableData: TableData) {
        binding.programProgress.visibility = View.GONE

        val tableView = TableView(requireContext()).apply {
            isShowHorizontalSeparators = false
            setHasFixedWidth(true)
            shadowColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            selectedColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            unSelectedColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        }

        val adapter = DataSetTableAdapter(
            abstracContext,
            presenterFragment.getProcessor(),
            presenterFragment.getProcessorOptionSet(),
            if (tableData.catCombo()?.isDefault == true) {
                getString(R.string.dataset_column_default)
            } else {
                null
            }
        ).apply {
            showColumnTotal = tableData.showColumnTotals
            showRowTotal = tableData.showRowTotals
            catCombo = tableData.catCombo()!!.uid()
            setTableView(tableView)
            initializeRows(tableData.accessDataWrite)
            setDataElementDecoration(presenter.dataSetHasDataElementDecoration())
        }

        adapters.add(adapter)

        val hasNumericDataElement = tableData.dataTableModel.rows
            ?.any { it.valueType()?.isNumeric == true } ?: false

        binding.tableLayout.addView(tableView)

        addSeparatorView()

        tableView.adapter = adapter
        tableView.headerCount = tableData.columnHeaders()!!.size

        adapter.swap(tableData.fieldViewModels)

        val overriddenWidth = tableData.overriddenMeasure.width
        val overrideHeight = tableData.overriddenMeasure.height

        if (overriddenWidth != 0) {
            adapter.setMaxLabel(tableData.maxLengthLabel())
            tableView.setRowHeaderWidth(overriddenWidth)
            adapter.columnHeaderHeight = overrideHeight
        } else {
            val widthFactor: Int = if (isPortrait()) {
                2
            } else {
                if (tableData.maxColumns() > 1) {
                    3
                } else {
                    2
                }
            }

            val (maxLabel, rowHeaderWidth, columnHeaderHeight) = tableData.rows()!!.measureText(
                requireContext(),
                widthFactor
            )
            adapter.setMaxLabel(maxLabel)
            tableView.setRowHeaderWidth(rowHeaderWidth)
            if (columnHeaderHeight != 0) {
                adapter.columnHeaderHeight = columnHeaderHeight +
                    requireContext().resources.getDimensionPixelSize(R.dimen.padding_5)
            }
            presenterFragment.saveCurrentSectionMeasures(
                adapter.rowHeaderWidth,
                adapter.columnHeaderHeight
            )
        }

        adapter.setAllItems(
            tableData.columnHeaders(),
            tableData.rows(),
            tableData.cells,
            adapter.showRowTotal && hasNumericDataElement
        )

        binding.scroll.setOnScrollChangeListener { _: NestedScrollView?,
            _: Int,
            scrollY: Int,
            _: Int,
            _: Int ->
            var position = -1
            if (checkTableHeights()) {
                for (i in heights.indices) {
                    if (scrollY < heights[i]) {
                        position = if (position == -1) i else position
                    }
                }
            }

            if (position != -1 && currentTablePosition.value != position) {
                currentTablePosition.value = position
            }
        }
        currentTablePosition.value = 0

        if (tablesCount == binding.tableLayout.childCount / 2) {
            presenterFragment.getDataSetIndicators()
        }
    }

    private fun addSeparatorView() {
        val view = View(context)
        view.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 15)
        view.setBackgroundResource(R.color.white)
        binding.tableLayout.addView(view)
    }

    override fun renderIndicators(indicators: SortedMap<String, String>) {
        binding.tableLayout.removeView(indicatorsTable)
        indicatorsTable = TableView(requireContext())
        val adapter = DataSetIndicatorAdapter(requireContext())
        indicatorsTable?.adapter = adapter
        indicatorsTable?.headerCount = 1
        indicatorsTable?.setPadding(0, 48.dp, 0, 48.dp)
        indicatorsTable?.clipToPadding = false
        val width = indicators.keys.toList().calculateWidth(requireContext()).second + 16.dp
        val max = resources.displayMetrics.widthPixels * 2 / 3
        indicatorsTable?.setRowHeaderWidth(if (width < max) width else max)
        adapter.setAllItems(
            listOf(listOf(getString(R.string.value))),
            indicators.keys.toList(),
            indicators.values.map { listOf(it) },
            false
        )
        binding.tableLayout.addView(indicatorsTable)
        binding.programProgress.visibility = View.GONE
    }

    override fun updateTabLayout(count: Int) {
        this.tablesCount = count
        activity.updateTabLayout()
    }

    private fun loadHeader(position: Int) {
        val tableView =
            (binding.scroll.getChildAt(0) as LinearLayout).getChildAt(position * 2) as TableView
        if (tableView != null) {
            val rvs = tableView.backupHeaders
            binding.headerContainer.removeAllViews()
            for (crv in rvs) {
                binding.headerContainer.addView(crv)
            }

            val cornerView =
                LayoutInflater.from(context).inflate(R.layout.table_view_corner_layout, null)
            val cornerParams = LinearLayout.LayoutParams(
                tableView.adapter.rowHeaderWidth,
                binding.headerContainer.children.toList().sumBy { it.layoutParams.height }
            )
            cornerView.layoutParams = cornerParams
            if (binding.headerContainer.childCount > 1) {
                cornerView.top =
                    (binding.headerContainer.childCount - 2) *
                    binding.headerContainer.getChildAt(0).layoutParams.height
            }

            val buttonAddWidth = cornerView.findViewById<View>(R.id.buttonRowScaleAdd)
            val buttonMinusWidth = cornerView.findViewById<View>(R.id.buttonRowScaleMinus)

            buttonAddWidth.setOnClickListener { resizeHeaderRowWidth(true, cornerView, rvs) }
            buttonMinusWidth.setOnClickListener { resizeHeaderRowWidth(false, cornerView, rvs) }

            binding.headerContainer.addView(cornerView)
        }
    }

    private fun resizeHeaderRowWidth(
        add: Boolean,
        cornerView: View,
        rvs: MutableList<CellRecyclerView>
    ) {
        for (i in 0 until binding.tableLayout.childCount) {
            if (binding.tableLayout.getChildAt(i) is TableView) {
                val table = binding.tableLayout.getChildAt(i) as TableView
                if (table.adapter is DataSetTableAdapter) {
                    val adapter = table.adapter as DataSetTableAdapter
                    adapter.scaleRowWidth(add)
                    val params = cornerView.layoutParams
                    params.width = adapter.rowHeaderWidth
                    cornerView.layoutParams = params
                    if (i == 0) {
                        presenterFragment.saveCurrentSectionMeasures(
                            adapter.rowHeaderWidth,
                            adapter.columnHeaderHeight
                        )
                        val scrollPos = table.scrollHandler.columnPosition
                        table.scrollToColumnPosition(scrollPos)
                        for (rv in rvs) {
                            rv.layoutManager!!.scrollToPosition(scrollPos)
                        }
                    }
                }
            }
        }
    }

    private fun checkTableHeights(): Boolean {
        if (heights.isEmpty()) {
            heights = ArrayList()

            for (i in 0 until (binding.scroll.getChildAt(0) as LinearLayout).childCount) {
                val view = (binding.scroll.getChildAt(0) as LinearLayout).getChildAt(i)
                if (view is TableView) {
                    if (i == (binding.scroll.getChildAt(0) as LinearLayout).childCount - 1) {
                        heights.add(
                            if (i != 0) {
                                heights[heights.size - 1] + view.getHeight()
                            } else {
                                view.getHeight()
                            }
                        )
                    } else {
                        val separator =
                            (binding.scroll.getChildAt(0) as LinearLayout)
                                .getChildAt(i + 1)
                        heights.add(
                            if (i / 2 != 0) {
                                heights[i / 2 - 1] + view.getHeight() + separator.height
                            } else {
                                view.getHeight() + separator.height
                            }
                        )
                    }
                }
            }
        }
        return heights.isNotEmpty()
    }

    override fun updateData(rowAction: RowAction, catCombo: String?) {
        for (adapter in adapters)
            if (adapter.catCombo == catCombo) {
                adapter.updateValue(rowAction)
                adapter.tableView.selectionHandler.clearIfCellSelected(
                    rowAction.rowPos(),
                    rowAction.columnPos()
                )
            }
    }

    override fun onValueProcessed() {
        if (activity.isBackPressed) {
            activity.abstractActivity.back()
        }
    }

    override fun clearTables() {
        binding.tableLayout.removeAllViews()
        adapters.clear()
    }

    override fun updateProgressVisibility() {
        when (binding.programProgress.visibility) {
            View.GONE -> binding.programProgress.visibility = View.VISIBLE
            View.VISIBLE -> binding.programProgress.visibility = View.GONE
        }
    }

    override fun showSnackBar() {
        saveToast.show()
    }

    override fun update(modified: Boolean) {
        if (modified) {
            activity.update()
        }
    }

    override fun showCalendar(dataElement: DataElement, cell: TableCell, showTimePicker: Boolean) {
        val dialog = CalendarPicker(binding.root.context)
        dialog.setTitle(dataElement.displayFormName())
        dialog.setInitialDate(cell.value?.toDate())
        dialog.isFutureDatesAllowed(true)
        dialog.setListener(object : OnDatePickerListener {
            override fun onNegativeClick() {
                presenterFragment.onCellValueChange(cell.copy(value = null))
            }

            override fun onPositiveClick(datePicker: DatePicker) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, datePicker.year)
                calendar.set(Calendar.MONTH, datePicker.month)
                calendar.set(Calendar.DAY_OF_MONTH, datePicker.dayOfMonth)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                val selectedDate: Date = calendar.time
                val result = DateUtils.oldUiDateFormat().format(selectedDate)
                if (showTimePicker) {
                    showTimePicker(dataElement, cell)
                } else {
                    presenterFragment.onCellValueChange(cell.copy(value = result))
                }
            }
        })
        dialog.show()
    }

    override fun showTimePicker(dataElement: DataElement, cell: TableCell) {
        val c = Calendar.getInstance()
        cell.value?.let { c.time = DateUtils.timeFormat().parse(it)!! }

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
                presenterFragment.onCellValueChange(cell.copy(value = calendarTime))
            },
            hour, minute, is24HourFormat
        )
        dialog.setTitle(dataElement.displayFormName())

        dialog.setButton(
            DialogInterface.BUTTON_NEGATIVE, requireContext().getString(R.string.date_dialog_clear)
        ) { _: DialogInterface?, _: Int ->
            presenterFragment.onCellValueChange(cell.copy(value = null))
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
                    presenterFragment.onCellValueChange(cell.copy(value = newValue))
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
                        presenterFragment.onCellValueChange(cell.copy(value = date))
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
                        presenterFragment.onCellValueChange(
                            cell.copy(value = coordinatesView.currentCoordinates())
                        )
                    }
                }

                override fun onNegative() {}
            },
            null
        ).show()
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
