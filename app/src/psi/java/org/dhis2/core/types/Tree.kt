package org.dhis2.core.types

typealias root<R> = Tree.Root<R>
typealias node<N> = Tree.Node<N>
typealias leaf<L> = Tree.Leaf<L>

sealed class Tree<T>(val content: T) {
    data class Root<R>(
        private val rootContent: R,
        val children: List<Tree<*>> = listOf(),
        var expanded: Boolean = false
    ) : Tree<R>(rootContent)

    data class Node<N>(
        private val nodeContent: N,
        val level: Int,
        val children: List<Tree<*>> = listOf(),
        val expanded: Boolean = false
    ) : Tree<N>(nodeContent)

    data class Leaf<L>(private val leafContent: L) : Tree<L>(leafContent)
}

fun <R> Tree.Root<R>.filter(predicate: (Tree<*>) -> Boolean): Tree.Root<R> {
    return this.copy(children = filterChildren(this.children, predicate))
}

private fun <R> Tree.Root<R>.filterChildren(
    oldChildren: List<Tree<*>>,
    predicate: (Tree<*>) -> Boolean
): List<Tree<*>> {

    val children = mutableListOf<Tree<*>>()

    for (child in oldChildren) {
        if (predicate(child)) {
            if (child is Tree.Node){
                children.add(
                    child.copy(children = filterChildren(child.children, predicate))
                )
            } else if (child is Tree.Leaf){
                children.add(child.copy())
            }
        }
    }

    return children
}

fun <R> Tree.Root<R>.expand(target: Tree<*>): Tree.Root<R> {
    return if (this == target) {
        this.copy(expanded = !this.expanded)
    } else {
        this.copy(children = expandChildren(this.children, target))
    }
}

private fun <R> Tree.Root<R>.expandChildren(
    oldChildren: List<Tree<*>>,
    target: Tree<*>
): List<Tree<*>> {

    val children = mutableListOf<Tree<*>>()

    for (child in oldChildren) {
        if (child is Tree.Node){
            if (child == target) {
                children.add(child.copy(expanded = !child.expanded))
            } else {
                children.add(child.copy(children = expandChildren(child.children, target)))
            }
        } else if (child is Tree.Leaf){
            children.add(child.copy())
        }
    }

    return children
}