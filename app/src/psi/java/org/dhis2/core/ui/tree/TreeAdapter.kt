package org.dhis2.core.ui.tree

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.core.types.TreeNode

class TreeAdapter(
    private val nodes: List<TreeNode<*>>,
    private val binders: List<TreeAdapterBinder>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private var displayNodes: MutableList<TreeNode<*>> = mutableListOf()
    private var levelByNodes = HashMap<TreeNode<*>, Int>()

    init {
        findDisplayNodes(nodes)
    }

    private fun findDisplayNodes(nodes: List<TreeNode<*>>, level: Int = 0) {
        for (node in nodes) {
            displayNodes.add(node)
            levelByNodes[node] = level

            if (node is TreeNode.Node && node.expanded) {
                findDisplayNodes(node.children, level + 1)
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

        val binder = binders.first() {
            it.contentJavaClass == node.content!!::class.java
        }

        return binder.layoutId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(viewType, parent, false)

        val viewHolder = binders.first {
            it.layoutId == viewType
        }.provideViewHolder(view)

        viewHolder.itemView.setOnClickListener {
            val node = displayNodes[viewHolder.adapterPosition]

            if (node is TreeNode.Node) {
                node.expanded = !node.expanded
                refresh()
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val node = displayNodes[position]
        val level = levelByNodes[node] ?: 0

        viewHolder.itemView.setPadding(level * 25, 0, 0, 0)

        binders.first() {
            it.contentJavaClass == node.content!!::class.java
        }.bindView(viewHolder, node)
    }
}
