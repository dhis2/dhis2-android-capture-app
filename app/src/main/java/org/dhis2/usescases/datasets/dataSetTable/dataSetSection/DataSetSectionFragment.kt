package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder.SelectionState.UNSELECTED
import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import java.util.ArrayList
import java.util.SortedMap
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.Bindings.calculateWidth
import org.dhis2.Bindings.dp
import org.dhis2.Bindings.measureText
import org.dhis2.R
import org.dhis2.data.forms.dataentry.tablefields.RowAction
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.FragmentDatasetSectionBinding
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableContract
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.Constants.ACCESS_DATA
import org.dhis2.utils.Constants.DATA_SET_SECTION
import org.dhis2.utils.Constants.DATA_SET_UID
import org.dhis2.utils.isPortrait
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.Section

class DataSetSectionFragment : FragmentGlobalAbstract(), DataValueContract.View {

    private lateinit var binding: FragmentDatasetSectionBinding
    private lateinit var activity: DataSetTableActivity
    private lateinit var presenter: DataSetTableContract.Presenter

    private val adapters = ArrayList<DataSetTableAdapter>()
    private lateinit var sectionName: String
    private lateinit var dataSetUid: String

    @Inject
    lateinit var presenterFragment: DataValuePresenter

    private var heights = ArrayList<Int>()
    private val currentTablePosition = MutableLiveData<Int>()
    private lateinit var dataSet: DataSet
    private lateinit var section: Section
    private var tablesCount: Int = 0
    private var indicatorsTable: TableView? = null
    private lateinit var saveToast: Toast

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        abstractActivity.hideKeyboard()
        requireView().clearFocus()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as DataSetTableActivity
        arguments?.let {
            dataSetUid = it.getString(DATA_SET_UID)
                ?: throw NullPointerException(
                    "dataSet should not be null. " +
                        "Before initializing the fragment make sure to set the correct arguments"
                )
        }
        app().userComponent()!!.plus(DataValueModule(dataSetUid, this)).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_dataset_section, container, false)
        currentTablePosition.observe(viewLifecycleOwner, Observer { loadHeader(it) })
        binding.presenter = presenterFragment
        presenter = activity.presenter
        sectionName = requireArguments().getString(DATA_SET_SECTION) ?: ""
        presenterFragment.init(
            this,
            presenter.orgUnitUid,
            presenter.periodTypeName,
            presenter.periodFinalDate,
            presenter.catCombo,
            sectionName,
            presenter.periodId
        )
        saveToast = Toast.makeText(requireContext(), R.string.datavalue_saved, Toast.LENGTH_SHORT)
        saveToast.setGravity(
            Gravity.TOP or Gravity.START,
            16.dp,
            110.dp
        )

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenterFragment.onDettach()
    }

    override fun setTableData(tableData: TableData) {
        binding.programProgress.visibility = View.GONE

        val adapter = DataSetTableAdapter(
            abstracContext,
            presenterFragment.getProcessor(),
            presenterFragment.getProcessorOptionSet(),
            if (tableData.catCombo()?.isDefault == true) {
                getString(R.string.dataset_column_default)
            } else {
                null
            }
        )
        adapters.add(adapter)

        val hasNumericDataElement = tableData.dataTableModel.rows()
            ?.any { it.valueType()?.isNumeric == true} ?: false

        adapter.apply {
            showColumnTotal = if (section.uid().isEmpty()) {
                false
            } else {
                section.showColumnTotals()
            }
            showRowTotal = if (section.uid().isEmpty()) {
                false
            } else {
                section.showRowTotals() == true && hasNumericDataElement
            }
        }

        val tableView = TableView(requireContext())
        tableView.isShowHorizontalSeparators = false
        tableView.setHasFixedWidth(true)

        val columnHeaders = tableData.columnHeaders()

        adapter.apply {
            catCombo = tableData.catCombo()!!.uid()
            setTableView(tableView)
            initializeRows(tableData.accessDataWrite)
            setDataElementDecoration(dataSet.dataElementDecoration())
        }

        binding.tableLayout.addView(tableView)

        val view = View(context)
        view.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 15)
        view.setBackgroundResource(R.color.white)
        binding.tableLayout.addView(view)

        tableView.adapter = adapter
        tableView.headerCount = columnHeaders!!.size
        tableView.shadowColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        tableView.selectedColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        tableView.unSelectedColor =
            ContextCompat.getColor(requireContext(), android.R.color.transparent)

        adapter.swap(tableData.fieldViewModels)

        val (first, second) = presenterFragment.getCurrentSectionMeasure()
        if (first != 0) {
            adapter.setMaxLabel(tableData.maxLengthLabel())
            tableView.setRowHeaderWidth(first)
            adapter.columnHeaderHeight = second
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

            val (first, second, third) = tableData.rows()!!.measureText(
                requireContext(),
                widthFactor
            )
            adapter.setMaxLabel(first)
            tableView.setRowHeaderWidth(second)
            if (third != 0) {
                adapter.columnHeaderHeight =
                    third + requireContext().resources.getDimensionPixelSize(R.dimen.padding_5)
            }
            presenterFragment.saveCurrentSectionMeasures(
                adapter.rowHeaderWidth,
                adapter.columnHeaderHeight
            )
        }

        adapter.setAllItems(
            columnHeaders,
            tableData.rows(),
            tableData.cells,
            adapter.showRowTotal && hasNumericDataElement
        )

        presenterFragment.initializeProcessor(this)

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
    }

    override fun setDataSet(dataSet: DataSet) {
        this.dataSet = dataSet
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

    override fun setSection(section: Section) {
        this.section = section
    }

    override fun updateTabLayout(count: Int) {
        this.tablesCount = count
        activity.updateTabLayout(sectionName, count)
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
            cornerView.findViewById<View>(R.id.buttonRowScaleAdd).setOnClickListener {
                for (i in 0 until binding.tableLayout.childCount) {
                    if (binding.tableLayout.getChildAt(i) is TableView) {
                        val table = binding.tableLayout.getChildAt(i) as TableView
                        if (table.adapter is DataSetTableAdapter) {
                            val adapter = table.adapter as DataSetTableAdapter
                            adapter.scaleRowWidth(true)
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
            cornerView.findViewById<View>(R.id.buttonRowScaleMinus).setOnClickListener { view ->
                for (i in 0 until binding.tableLayout.childCount) {
                    if (binding.tableLayout.getChildAt(i) is TableView) {
                        val table = binding.tableLayout.getChildAt(i) as TableView
                        if (table.adapter is DataSetTableAdapter) {
                            val adapter = table.adapter as DataSetTableAdapter
                            adapter.scaleRowWidth(false)
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
            binding.headerContainer.addView(cornerView)
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

    fun rowActions(): Flowable<RowAction> {
        return adapters[0].asFlowable()
    }

    fun optionSetActions(): FlowableProcessor<Trio<String, String, Int>> {
        return adapters[0].asFlowableOptionSet()
    }

    fun updateData(rowAction: RowAction, catCombo: String?) {
        for (adapter in adapters)
            if (adapter.catCombo == catCombo) {
                adapter.updateValue(rowAction)
            }
    }

    override fun showSnackBar() {
        saveToast.show()
    }

    override fun goToTable(numTable: Int) {
        binding.scroll.scrollTo(0, binding.tableLayout.getChildAt(numTable * 2).top)
    }

    fun currentNumTables(): Int {
        return tablesCount
    }

    override fun showAlertDialog(title: String, message: String) {
        super.showInfoDialog(title, message)
    }

    override fun highligthHeaderRow(table: Int, row: Int, mandatory: Boolean) {
        val columnHeader = adapters[table].tableView.rowHeaderRecyclerView
            .findViewHolderForAdapterPosition(row) as AbstractViewHolder?

        if (columnHeader != null) {
            columnHeader.setSelected(UNSELECTED)
            columnHeader.setBackgroundColor(
                if (mandatory) {
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.table_view_default_mandatory_background_color
                    )
                } else {
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.table_view_default_all_required_background_color
                    )
                }
            )
        }
    }

    override fun update(modified: Boolean) {
        if (modified) {
            activity.update()
        }
    }

    companion object {

        @JvmStatic
        fun create(
            sectionUid: String,
            accessDataWrite: Boolean,
            dataSetUid: String
        ): DataSetSectionFragment {
            val bundle = Bundle()
            bundle.putString(DATA_SET_SECTION, sectionUid)
            bundle.putBoolean(ACCESS_DATA, accessDataWrite)
            bundle.putString(DATA_SET_UID, dataSetUid)
            val dataSetSectionFragment = DataSetSectionFragment()
            dataSetSectionFragment.arguments = bundle
            return dataSetSectionFragment
        }
    }
}
