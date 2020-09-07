package org.dhis2.core.types

sealed class TreeNode<T>(val content: T) {
    var parent: TreeNode<*>? = null

    val level: Int
        get() {
            return if (parent == null) {
                0
            } else {
                val parentLevel = parent!!.level
                parentLevel + 1
            }
        }

    class Node<T>(
        content: T,
        initialChildren: List<TreeNode<*>> = mutableListOf(),
        var expanded: Boolean = false
    ) : TreeNode<T>(content) {
        private val internalChildren: MutableList<TreeNode<*>> = initialChildren.toMutableList()

        init {
            children.forEach {
                it.parent = this
            }
        }

        val children: List<TreeNode<*>>
            get() {
                return internalChildren.toList()
            }

        fun addChild(node:TreeNode<*>){
            internalChildren.add(node)
            node.parent = this
        }
    }

    class Leaf<T>(content: T) : TreeNode<T>(content)
}