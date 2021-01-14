package dhis2.org.analytics.charts.table

import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import dhis2.org.R
import kotlinx.android.synthetic.main.item_table_cell.view.text

class GraphTableHolder(itemView: View) : AbstractViewHolder(itemView) {
    fun bind(text: String, isHeader: Boolean) {
        itemView.text.let {
            it.text = text
            if (isHeader) {
                ViewCompat.setBackgroundTintList(
                    it,
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.table_view_default_header_background_color
                        )
                    )
                )
                it.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.table_view_default_text_color
                    )
                )
            }
        }
    }
}
