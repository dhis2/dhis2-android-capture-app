package dhis2.org.analytics.charts.table

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import dhis2.org.R

class GraphTableAdapter(context: Context) : AbstractTableAdapter<String, String, String>(context) {

    override fun getCellItemViewType(columnPosition: Int, rowPosition: Int): Int = 0

    override fun onCreateCornerView(): View? = null

    override fun getColumnHeaderItemViewType(position: Int): Int = 0

    override fun getRowHeaderItemViewType(position: Int): Int = 0

    override fun onCreateColumnHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder = GraphTableHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_table_header, parent, false)
    )

    override fun onBindColumnHeaderViewHolder(
        holder: AbstractViewHolder?,
        columnHeaderItemModel: Any?,
        columnPosition: Int
    ) {
        (holder as GraphTableHolder).bind(columnHeaderItemModel.toString())
    }

    override fun onCreateRowHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder = GraphTableHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_table_header, parent, false)
    )
    override fun onBindRowHeaderViewHolder(
        holder: AbstractViewHolder?,
        rowHeaderItemModel: Any?,
        rowPosition: Int
    ) {
        (holder as GraphTableHolder).bind(rowHeaderItemModel.toString())
    }

    override fun onCreateCellViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder = GraphTableHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_table_cell, parent, false)
    )

    override fun onBindCellViewHolder(
        holder: AbstractViewHolder?,
        cellItemModel: Any?,
        columnPosition: Int,
        rowPosition: Int
    ) {
        (holder as GraphTableHolder).bind(cellItemModel.toString())
    }
}
