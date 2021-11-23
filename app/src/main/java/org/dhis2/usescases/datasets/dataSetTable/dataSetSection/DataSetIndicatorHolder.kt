package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import android.view.View
import android.widget.TextView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import dhis2.org.R

class DataSetIndicatorHolder(itemView: View) : AbstractViewHolder(itemView) {
    fun bind(text: String) {
        itemView.findViewById<TextView>(R.id.text).text = text
    }

    override fun setBackgroundColor(p_nColor: Int) {
        /*Unused to avoid color change*/
    }

    override fun setSelected(selectionState: SelectionState?) {
        /*Unused to avoid color change*/
    }
}
