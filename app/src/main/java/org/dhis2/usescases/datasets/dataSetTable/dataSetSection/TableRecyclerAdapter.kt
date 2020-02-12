package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.TableView
import org.dhis2.Bindings.getMaxWidth
import org.dhis2.Bindings.px
import org.dhis2.databinding.ItemProgressBinding
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.ProgressViewHolder
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.Section

const val TABLE_TYPE = 0
const val LOADING_TYPE = 1
const val progressId = 1987L

class TableRecyclerAdapter(
    private val context: Context,
    private val dataSet: DataSet,
    private val section: Section,
    private var tableList: MutableList<DataSetTable> = ArrayList(),
    var adapterList: MutableList<DataSetTableAdapter> = ArrayList(),
    private val widthSelectorListener: TableView.OnWidthSelectorListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isLoading = true
    var tables: MutableMap<Int, TableView> = mutableMapOf()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TABLE_TYPE -> {
                val tableView = TableView(parent.context)
                tableView.setHasFixedWidth(true)
                tableView.shadowColor = ColorUtils.getPrimaryColor(
                    context,
                    ColorUtils.ColorType.PRIMARY
                )
                tableView.addOnWidthSelectorListener(widthSelectorListener)
                TableViewHolder(tableView)
            }
            else ->
                ProgressViewHolder(ItemProgressBinding.inflate(LayoutInflater.from(parent.context)))
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) {
            tableList.size + 1
        } else {
            tableList.size
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TableViewHolder -> bindTableViewHolder(holder, position)
            is ProgressViewHolder -> bindProgressViewHolder(holder)
            else ->
                throw IllegalArgumentException(
                    "%s is not supported".format(holder.javaClass.simpleName)
                )
        }
    }

    private fun bindTableViewHolder(holder: TableViewHolder, position: Int) {
        if (tables[position] == null) {
            tables[position] = holder.tableView

            val dataSetTable = tableList[position]
            val dataTableModel = dataSetTable.dataTableModel
            val accessDataWrite = dataSetTable.accessDataWrite

            val list = dataTableModel.rows.map { it.displayName() ?: "" }
            val desiredWidth = list.getMaxWidth(
                13.px.toFloat(),
                Typeface.DEFAULT
            ) + 10.px + 18.px
            holder.tableView.setRowHeaderWidth(
                when {
                    desiredWidth < Resources.getSystem().displayMetrics.widthPixels / 3 -> {
                        desiredWidth
                    }
                    desiredWidth / 2 < Resources.getSystem().displayMetrics.widthPixels / 3 -> {
                        desiredWidth / 2
                    }
                    else -> {
                        Resources.getSystem().displayMetrics.widthPixels / 3
                    }
                }
            )

            val adapter = adapterList[position]

            adapter.showColumnTotal =
                if (section.uid().isEmpty()) false else section.showColumnTotals()
            adapter.showRowTotal = if (section.uid().isEmpty()) false else section.showRowTotals()


            val columnHeaders: MutableList<MutableList<CategoryOption>>? = dataTableModel.header
            adapter.catCombo = dataTableModel.catCombo.uid()
            adapter.setTableView(holder.tableView)
            adapter.initializeRows(accessDataWrite)
            adapter.setDataElementDecoration(dataSet.dataElementDecoration())

            holder.tableView.adapter = adapter
            holder.tableView.headerCount = columnHeaders?.size!!

            adapter.swap(dataSetTable.fields)

            adapter.setAllItems(
                columnHeaders,
                dataTableModel.rows,
                dataSetTable.cells,
                adapter.showRowTotal
            )
        }
    }

    private fun bindProgressViewHolder(holder: ProgressViewHolder) {
        //Not in use
    }


    override fun getItemViewType(position: Int): Int {
        return if (isLoading && position == itemCount - 1) {
            LOADING_TYPE
        } else {
            TABLE_TYPE
        }
    }

    override fun getItemId(position: Int): Long {
        return if (isLoading && position == itemCount - 1) {
            progressId
        } else {
            tableList[position].dataTableModel.catCombo.uid().hashCode().toLong()
        }
    }

    fun addTable(dataSetTable: DataSetTable, adapter: DataSetTableAdapter) {
        tableList.add(dataSetTable)
        adapterList.add(adapter)
        notifyDataSetChanged()
    }

    fun finishLoading() {
        isLoading = false
    }
}