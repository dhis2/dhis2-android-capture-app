package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.core.types.Tree
import org.dhis2.core.ui.tree.TreeAdapterBinder

class FeedbackItemBinder : TreeAdapterBinder(FeedbackItem::class.java) {
    override val layoutId: Int
        get() = R.layout.item_feedback

    override fun provideViewHolder(itemView: View): RecyclerView.ViewHolder {
        return ViewHolder(itemView)
    }

    override fun bindView(
        holder: RecyclerView.ViewHolder,
        node: Tree<*>
    ) {
        with(holder as ViewHolder) {
            val feedbackItem: FeedbackItem = node.content as FeedbackItem

            renderColor(itemView, node)
            renderName(name, feedbackItem, node)
            renderValue(value, feedbackItem)
            renderArrow(arrow, node)
        }
    }

    private fun renderColor(
        itemView: View,
        node: Tree<*>
    ) {
        if (node is Tree.Leaf || (node is Tree.Node && node.children.isEmpty())) {
            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.bg_gray_faf
                )
            )
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun renderName(
        nameView: TextView,
        feedbackItem: FeedbackItem,
        node: Tree<*>
    ) {

        val percentage =
            if (feedbackItem.value == null || feedbackItem.value.data.isNullOrBlank()) 0.9f else 0.6f

        (nameView.layoutParams as ConstraintLayout.LayoutParams)
            .matchConstraintPercentWidth = percentage
        nameView.requestLayout()

        if (node is Tree.Node && node.children.isNotEmpty() && node.children[0] is Tree.Node) {
            TextViewCompat.setTextAppearance(
                nameView,
                R.style.TextAppearance_MaterialComponents_Body1
            )
        } else {
            TextViewCompat.setTextAppearance(
                nameView,
                R.style.TextAppearance_MaterialComponents_Body2
            )
        }

        nameView.text = (feedbackItem.name)
    }

    private fun renderValue(
        valueView: TextView,
        feedbackItem: FeedbackItem
    ) {
        if (feedbackItem.value == null || feedbackItem.value.data.isNullOrBlank()) {
            valueView.visibility = View.GONE
        } else {

            if (feedbackItem.value.isNumeric) {
                val num = feedbackItem.value.data.toDouble()

                valueView.text = "%.1f".format(num)
            } else {
                valueView.text = feedbackItem.value.data
            }

            if (feedbackItem.value.color != null) {
                valueView.setBackgroundColor(Color.parseColor(feedbackItem.value.color))
            } else {
                valueView.setBackgroundColor(Color.TRANSPARENT)
            }

            valueView.visibility = View.VISIBLE
        }
    }

    private fun renderArrow(
        arrow: ImageView,
        node: Tree<*>
    ) {
        if (node is Tree.Node && node.expanded) {
            arrow.setImageResource(R.drawable.ic_arrow_up)
        } else {
            arrow.setImageResource(R.drawable.ic_arrow_down)
        }

        arrow.visibility =
            if (node is Tree.Node && node.children.isNotEmpty()) View.VISIBLE else View.INVISIBLE
    }

    internal class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val name: TextView = rootView.findViewById(R.id.name)
        val value: TextView = rootView.findViewById(R.id.value)
        val arrow: ImageView = rootView.findViewById(R.id.arrow)
    }
}