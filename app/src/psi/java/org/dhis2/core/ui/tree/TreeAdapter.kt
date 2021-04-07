package org.dhis2.core.ui.tree

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.core.types.Tree

class TreeAdapter(
    private val binders: List<TreeAdapterBinder>,
    private val onTreeClickListener: (node: Tree<*>) -> Unit,
    private val displayRoot: Boolean = false
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private var displayNodes: MutableList<Tree<*>> = mutableListOf()
    private var levelByNodes = HashMap<Tree<*>, Int>()

    fun refresh(root: Tree.Root<*>) {
        displayNodes.clear()
        levelByNodes.clear()
        findDisplayNodes(root)
        notifyDataSetChanged()
    }

    private fun findDisplayNodes(root: Tree.Root<*>) {
        if (displayRoot) {
            displayNodes.add(root)
            levelByNodes[root] = 0

            findDisplayNodes(root.children, 1)
        } else {
            findDisplayNodes(root.children)
        }
    }

    private fun findDisplayNodes(nodes: List<Tree<*>>, level: Int = 0) {
        for (node in nodes) {
            displayNodes.add(node)
            levelByNodes[node] = level

            if (node is Tree.Node && node.expanded) {
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

            if (node is Tree.Node) {
                onTreeClickListener(node)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val node = displayNodes[position]
        val level = levelByNodes[node] ?: 0

        val padding = getFixedPadding(viewHolder)

        viewHolder.itemView.setPadding(level * padding, 0, 0, 0)

        binders.first() {
            it.contentJavaClass == node.content!!::class.java
        }.bindView(viewHolder, node)
    }

    private fun getFixedPadding(viewHolder: RecyclerView.ViewHolder): Int {
        val scale: Float = viewHolder.itemView.resources.displayMetrics.density
        return (12 * scale + 0.5f).toInt()
    }
}
