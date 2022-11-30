package dhis2.org.analytics.charts.table

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import dhis2.org.R

class GraphTableHolder(itemView: View) : AbstractViewHolder(itemView) {
    fun bind(model: CellModel) {
        val textView = itemView.findViewById<TextView>(R.id.text)
        textView.text = model.text

        if (model.color != null) {
            textView.setBackgroundColor(model.color)
        } else {
            textView.setBackgroundColor(
                (ContextCompat.getColor(itemView.context, R.color.table_view_default_text_color))
            )
        }

        textView.text = model.text
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
