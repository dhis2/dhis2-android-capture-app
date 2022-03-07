package dhis2.org.analytics.charts.table

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import dhis2.org.R

class GraphTableAdapter(val context: Context) :
    AbstractTableAdapter<CellModel, CellModel, CellModel>(context) {

    private val currentWidth = 200

    override fun getCellItemViewType(columnPosition: Int, rowPosition: Int): Int = 0

    override fun onCreateCornerView(): View? {
        val layout = com.evrencoskun.tableview.R.layout.default_cornerview_layout
        return LayoutInflater.from(context).inflate(layout, null)
    }

    override fun getColumnHeaderItemViewType(position: Int): Int = 0

    override fun getRowHeaderItemViewType(position: Int): Int = 0

    override fun onCreateColumnHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder = GraphTableHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.analytics_table_header, parent, false)
    )

    override fun onBindColumnHeaderViewHolder(
        holder: AbstractViewHolder?,
        columnHeaderItemModel: Any?,
        columnPosition: Int
    ) {
        (holder as GraphTableHolder).bind(columnHeaderItemModel as CellModel)
        holder.setBackground(columnPosition % 2 == 0)
        val i = getHeaderRecyclerPositionFor(columnHeaderItemModel)
        holder.itemView.updateLayoutParams {
            width = currentWidth * i + (context.resources.displayMetrics.density * (i - 1)).toInt()
        }
    }

    override fun onCreateRowHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder = GraphTableHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.analytics_table_fixed_header, parent, false)
    )

    override fun onBindRowHeaderViewHolder(
        holder: AbstractViewHolder?,
        rowHeaderItemModel: Any?,
        rowPosition: Int
    ) {
        (holder as GraphTableHolder).bind(rowHeaderItemModel as CellModel)
    }

    override fun onCreateCellViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder = GraphTableHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.analytics_table_cell, parent, false)
    )

    override fun onBindCellViewHolder(
        holder: AbstractViewHolder?,
        cellItemModel: Any?,
        columnPosition: Int,
        rowPosition: Int
    ) {
        (holder as GraphTableHolder).bind(cellItemModel as CellModel)
        holder.itemView.updateLayoutParams {
            width = this@GraphTableAdapter.currentWidth
        }
    }
}
