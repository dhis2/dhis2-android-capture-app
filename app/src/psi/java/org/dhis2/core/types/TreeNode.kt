package org.dhis2.core.types

typealias node<N> = TreeNode.Node<N>
typealias leaf<L> = TreeNode.Leaf<L>

sealed class TreeNode<T>(val content: T) {
    data class Node<N>(
        private val nodeContent: N,
        val children: List<TreeNode<*>> = mutableListOf(),
        var expanded: Boolean = false
    ) : TreeNode<N>(nodeContent)

    data class Leaf<L>(private val leafContent: L) : TreeNode<L>(leafContent)
}