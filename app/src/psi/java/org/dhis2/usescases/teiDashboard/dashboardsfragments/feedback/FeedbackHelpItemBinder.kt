package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import org.dhis2.R
import org.dhis2.core.ui.tree.TreeAdapterBinder
import org.dhis2.core.types.Tree

class FeedbackHelpItemBinder : TreeAdapterBinder(FeedbackHelpItem::class.java) {
    override val layoutId: Int
        get() = R.layout.item_help_feedback

    override fun provideViewHolder(itemView: View): RecyclerView.ViewHolder {
        return ViewHolder(itemView)
    }

    override fun bindView(
        holder: RecyclerView.ViewHolder,
        node: Tree<*>
    ) {
        with(holder as ViewHolder) {
            val feedbackHelpItem: FeedbackHelpItem = node.content as FeedbackHelpItem

            val markwon = Markwon.create(itemView.context)
            markwon.setMarkdown(helpText, feedbackHelpItem.text)

            refreshShowingAll(holder, feedbackHelpItem)

            helpText.setOnClickListener {
                expandOrCollapse(holder, feedbackHelpItem)
            }

            arrow.setOnClickListener {
                expandOrCollapse(holder, feedbackHelpItem)
            }
        }
    }

    private fun expandOrCollapse(
        holder: RecyclerView.ViewHolder,
        feedbackHelpItem: FeedbackHelpItem
    ) {
        feedbackHelpItem.showingAll = !feedbackHelpItem.showingAll
        refreshShowingAll(holder, feedbackHelpItem)
    }

    private fun refreshShowingAll(
        holder: RecyclerView.ViewHolder,
        feedbackHelpItem: FeedbackHelpItem
    ) {
        with(holder as ViewHolder) {
            helpText.maxLines = if (feedbackHelpItem.showingAll) Int.MAX_VALUE else 2
            if (feedbackHelpItem.showingAll) {
                arrow.setImageResource(R.drawable.ic_arrow_upward)
            } else {
                arrow.setImageResource(R.drawable.ic_arrow_downward)
            }
        }
    }

    internal class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val helpText: TextView = rootView.findViewById(R.id.help_text)
        val arrow: ImageView = rootView.findViewById(R.id.arrow)
    }
}