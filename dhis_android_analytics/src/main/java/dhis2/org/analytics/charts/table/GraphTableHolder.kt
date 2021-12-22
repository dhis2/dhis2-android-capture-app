package dhis2.org.analytics.charts.table

import android.view.View
import android.widget.TextView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import dhis2.org.R

class GraphTableHolder(itemView: View) : AbstractViewHolder(itemView) {
    fun bind(text: String) {
        itemView.findViewById<TextView>(R.id.text).text = text
        itemView.findViewById<TextView>(R.id.text).isSelected = true
    }

    fun setBackground(isEven: Boolean) {
        itemView.findViewById<View>(R.id.root)?.setBackgroundResource(
            if (isEven) {
                R.color.even_header_color
            } else {
                R.color.odd_header_color
            }
        )
    }
}
