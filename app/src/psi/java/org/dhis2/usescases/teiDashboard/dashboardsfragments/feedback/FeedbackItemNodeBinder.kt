package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.graphics.Color
import android.view.View
import android.widget.ImageView

import android.widget.TextView
import androidx.core.widget.TextViewCompat
import org.dhis2.R
import tellh.com.recyclertreeview_lib.TreeNode

import tellh.com.recyclertreeview_lib.TreeViewBinder

class FeedbackItemNodeBinder : TreeViewBinder<FeedbackItemNodeBinder.ViewHolder>() {
    override fun provideViewHolder(itemView: View): ViewHolder {
        return ViewHolder(itemView)
    }

    override fun bindView(holder: ViewHolder, position: Int, node: TreeNode<*>) {
        val feedbackItem: FeedbackItem = node.content as FeedbackItem
        holder.name.text = (feedbackItem.name)

        if (node.isRoot){
            TextViewCompat.setTextAppearance(holder.name, R.style.TextAppearance_MaterialComponents_Body1)
        } else {
            TextViewCompat.setTextAppearance(holder.name, R.style.TextAppearance_MaterialComponents_Body2)
        }

        if (feedbackItem.value == null || feedbackItem.value.data.isNullOrBlank()) {
            holder.value.visibility = View.GONE
        } else {
            holder.value.text = feedbackItem.value.data
            holder.value.setBackgroundColor(Color.parseColor(feedbackItem.value.color))
            holder.value.visibility = View.VISIBLE
        }

        holder.arrow.visibility = if (node.isLeaf) View.INVISIBLE else View.VISIBLE
    }

    override fun getLayoutId(): Int {
        return R.layout.item_feedback
    }

    class ViewHolder(rootView: View) : TreeViewBinder.ViewHolder(rootView) {
        val name: TextView
        val value: TextView
        val arrow: ImageView

        init {
            name = rootView.findViewById(R.id.name)
            value = rootView.findViewById(R.id.value)
            arrow = rootView.findViewById(R.id.arrow)
        }
    }
}