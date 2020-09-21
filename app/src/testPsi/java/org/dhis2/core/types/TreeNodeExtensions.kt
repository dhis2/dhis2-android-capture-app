package org.dhis2.core.types

fun <N> TreeNode.Node<N>.node(content: N, initialize: (TreeNode.Node<N>.() -> Unit)? = null) {
    val child = TreeNode.Node(content)

    addChild(child)
    if (initialize != null) {
        child.initialize()
    }
}

fun <L,N> TreeNode.Node<N>.leaf(content: L) {
    addChild(TreeNode.Leaf(content))
}

fun <N> root(content: N, initialize: (TreeNode.Node<N>.() -> Unit)? = null): TreeNode.Node<N> {
    val node = TreeNode.Node(content)
    if (initialize != null) {
        node.initialize()
    }
    return node
}
