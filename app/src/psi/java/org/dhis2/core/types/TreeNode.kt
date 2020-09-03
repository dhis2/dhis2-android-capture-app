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

    class Branch<T>(
        content: T,
        val children: List<TreeNode<*>> = listOf(),
        var expanded: Boolean = false
    ) : TreeNode<T>(content) {
        init {
            children.forEach {
                it.parent = this
            }
        }
    }

    class Leaf<T>(content: T) : TreeNode<T>(content)
}