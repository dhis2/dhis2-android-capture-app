package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.core.ui.tree.TreeAdapterBinder
import org.dhis2.core.types.TreeNode

class FeedbackItemBinder : TreeAdapterBinder(FeedbackItem::class.java) {
    override val layoutId: Int
        get() = R.layout.item_feedback

    override fun provideViewHolder(itemView: View): RecyclerView.ViewHolder {
        return ViewHolder(itemView)
    }

    override fun bindView(
        holder: RecyclerView.ViewHolder,
        node: TreeNode<*>
    ) {
        val branch = node as TreeNode.Branch

        with(holder as ViewHolder) {
            val feedbackItem: FeedbackItem = node.content as FeedbackItem
            name.text = (feedbackItem.name)

            if (node.children.isNotEmpty() && node.children[0] is TreeNode.Branch) {
                TextViewCompat.setTextAppearance(
                    name,
                    R.style.TextAppearance_MaterialComponents_Body1
                )
            } else {
                TextViewCompat.setTextAppearance(
                    name,
                    R.style.TextAppearance_MaterialComponents_Body2
                )
            }

            if (feedbackItem.value == null || feedbackItem.value.data.isNullOrBlank()) {
                value.visibility = View.GONE
            } else {
                value.text = feedbackItem.value.data

                if (feedbackItem.value.color!= null){
                    value.setBackgroundColor(Color.parseColor(feedbackItem.value.color))
                }

                value.visibility = View.VISIBLE
            }

            if (node.expanded) {
                arrow.setImageResource(R.drawable.ic_arrow_up)
            } else {
                arrow.setImageResource(R.drawable.ic_arrow_down)
            }

            arrow.visibility = if (node.children.isEmpty()) View.INVISIBLE else View.VISIBLE
        }
    }

    internal class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val name: TextView = rootView.findViewById(R.id.name)
        val value: TextView = rootView.findViewById(R.id.value)
        val arrow: ImageView = rootView.findViewById(R.id.arrow)
    }
}