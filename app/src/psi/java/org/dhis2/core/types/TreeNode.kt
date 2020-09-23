package org.dhis2.core.types

typealias root<R> = TreeNode.Root<R>
typealias node<N> = TreeNode.Node<N>
typealias leaf<L> = TreeNode.Leaf<L>

sealed class TreeNode<T>(val content: T) {
    data class Root<R>(
        private val rootContent: R,
        val children: List<TreeNode<*>> = listOf(),
        var expanded: Boolean = false
    ) : TreeNode<R>(rootContent)

    data class Node<N>(
        private val nodeContent: N,
        val children: List<TreeNode<*>> = listOf(),
        val expanded: Boolean = false
    ) : TreeNode<N>(nodeContent)

    data class Leaf<L>(private val leafContent: L) : TreeNode<L>(leafContent)
}

fun <R> TreeNode.Root<R>.expand(target: TreeNode<*>): TreeNode.Root<R> {
    return if (this == target) {
        this.copy(expanded = !this.expanded)
    } else {
        this.copy(children = expandChildren(this.children, target))
    }
}

private fun <R> TreeNode.Root<R>.expandChildren(
    children: List<TreeNode<*>>,
    target: TreeNode<*>
): List<TreeNode<*>> {

    val newChildren = mutableListOf<TreeNode<*>>()

    for (child in children) {
        when (child) {
            is TreeNode.Node -> {
                if (child == target) {
                    newChildren.add(child.copy(expanded = !child.expanded))
                } else {
                    newChildren.add(
                        child.copy(
                            children = expandChildren(
                                child.children,
                                target
                            )
                        )
                    )
                }
            }
            is TreeNode.Leaf -> newChildren.add(child.copy())
        }
    }

    return newChildren
}