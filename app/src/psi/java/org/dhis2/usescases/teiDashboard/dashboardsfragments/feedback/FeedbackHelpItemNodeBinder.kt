package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.view.View
import android.widget.ImageView

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import org.dhis2.R
import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewAdapter

import tellh.com.recyclertreeview_lib.TreeViewBinder

class FeedbackHelpItemNodeBinder : TreeViewBinder<FeedbackHelpItemNodeBinder.ViewHolder>() {
    override fun provideViewHolder(itemView: View): ViewHolder {
        return ViewHolder(itemView)
    }

    override fun bindView(holder: ViewHolder, position: Int, node: TreeNode<*>) {
        val feedbackHelpItem: FeedbackHelpItem = node.content as FeedbackHelpItem

        val markwon = Markwon.create(holder.itemView.context);
        markwon.setMarkdown(holder.helpText, feedbackHelpItem.text);

        holder.arrow.setOnClickListener {
            val isExpand = holder.helpText.maxLines == Int.MAX_VALUE

            holder.helpText.maxLines = if (isExpand) 2 else Int.MAX_VALUE
            val rotateDegree = if (isExpand) 180 else -180
            holder.arrow.animate().rotationBy(rotateDegree.toFloat()).start()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.item_help_feedback
    }

    class ViewHolder(rootView: View) : TreeViewBinder.ViewHolder(rootView) {
        val helpText: TextView
        val arrow: ImageView

        init {
            helpText = rootView.findViewById(R.id.help_text)
            arrow = rootView.findViewById(R.id.arrow)
        }
    }
}