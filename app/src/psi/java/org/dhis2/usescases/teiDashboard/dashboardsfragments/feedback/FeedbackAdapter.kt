package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import org.dhis2.R

class FeedbackAdapter(private val nodes: List<TreeNode<*>>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private var displayNodes: MutableList<TreeNode<*>> = mutableListOf()

    init {
        findDisplayNodes(nodes)
    }

    private fun findDisplayNodes(nodes: List<TreeNode<*>>) {
        for (node in nodes) {
            displayNodes.add(node)

            if (node is TreeNode.Branch && node.expanded){
                findDisplayNodes(node.children)
            }
        }
    }

    private fun refresh() {
        displayNodes.clear()
        findDisplayNodes(nodes)
        notifyDataSetChanged()
    }

    override fun getItemCount() = displayNodes.size

    override fun getItemViewType(position: Int): Int {
        val node = displayNodes[position]
        return if (node.content is FeedbackItem) {
            FEEDBACK_ITEM
        } else {
            FEEDBACK_HELP_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View

        val viewHolder =  when (viewType) {
            FEEDBACK_ITEM -> {
                itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_feedback, parent, false)
                ItemViewHolder(itemView)
            }
            else -> {
                itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_help_feedback, parent, false)
                ItemHelpViewHolder(itemView)
            }
        }

        viewHolder.itemView.setOnClickListener {
            val node = displayNodes[viewHolder.adapterPosition]

            if (node is TreeNode.Branch){
                node.expanded = !node.expanded
                refresh()
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        val node = displayNodes[position]


        viewHolder.itemView.setPadding( node.level * 25,0,0,0)

        when (getItemViewType(position)) {
            FEEDBACK_ITEM -> (viewHolder as ItemViewHolder).bindView(node as TreeNode.Branch<*>)
            FEEDBACK_HELP_ITEM -> (viewHolder as ItemHelpViewHolder).bindView(node as TreeNode.Leaf<*>)
        }
    }

    internal class ItemViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        private val name: TextView = rootView.findViewById(R.id.name)
        private val value: TextView = rootView.findViewById(R.id.value)
        private val arrow: ImageView = rootView.findViewById(R.id.arrow)

        fun bindView(node: TreeNode.Branch<*>) {
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
                value.setBackgroundColor(Color.parseColor(feedbackItem.value.color))
                value.visibility = View.VISIBLE
            }

            if (node.expanded) {
              arrow.setImageResource(R.drawable.ic_arrow_up)
            } else{
                arrow.setImageResource(R.drawable.ic_arrow_down)
            }

            arrow.visibility = if (node.children.isEmpty()) View.INVISIBLE else View.VISIBLE
        }
    }

    internal class ItemHelpViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        private val helpText: TextView = rootView.findViewById(R.id.help_text)
        private val arrow: ImageView = rootView.findViewById(R.id.arrow)

        fun bindView(node: TreeNode.Leaf<*>) {
            val feedbackHelpItem: FeedbackHelpItem = node.content as FeedbackHelpItem

            val markwon = Markwon.create(itemView.context);
            markwon.setMarkdown(helpText, feedbackHelpItem.text);

            arrow.setOnClickListener {
                val isExpand = helpText.maxLines == Int.MAX_VALUE

                helpText.maxLines = if (isExpand) 2 else Int.MAX_VALUE
                val rotateDegree = if (isExpand) 180 else -180
                arrow.animate().rotationBy(rotateDegree.toFloat()).start()
            }
        }
    }

    companion object {
        private const val FEEDBACK_ITEM = 1
        private const val FEEDBACK_HELP_ITEM = 2
    }
}
