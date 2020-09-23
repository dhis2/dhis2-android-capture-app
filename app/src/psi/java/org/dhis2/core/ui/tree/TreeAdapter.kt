package org.dhis2.core.ui.tree

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.core.types.TreeNode
import org.dhis2.core.types.expand

class TreeAdapter(
    root: TreeNode.Root<*>,
    private val binders: List<TreeAdapterBinder>,
    private val onTreeClickListener: (TreeNode<*>) -> Unit,
    private val displayRoot: Boolean = false
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private var displayNodes: MutableList<TreeNode<*>> = mutableListOf()
    private var levelByNodes = HashMap<TreeNode<*>, Int>()

    init {
        findDisplayNodes(root)
    }

    private fun findDisplayNodes(root: TreeNode.Root<*>) {
        if (displayRoot) {
            displayNodes.add(root)
            levelByNodes[root] = 0

            findDisplayNodes(root.children, 1)
        } else {
            findDisplayNodes(root.children)
        }
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
                onTreeClickListener(node)
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
