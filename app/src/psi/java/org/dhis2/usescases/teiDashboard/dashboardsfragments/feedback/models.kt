package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

enum class ProgramType { HNQIS, RDQA }

data class FeedbackProgram(val uid: String, val programType: ProgramType)

data class FeedbackItemValue(val data: String?, val color: String)

data class FeedbackItem(val name: String, val value: FeedbackItemValue? = null)

data class FeedbackHelpItem(val text: String, var showingAll: Boolean = false)

sealed class TreeNode<T>(val content: T) {
    var parent: TreeNode<*>? = null

    val level: Int
        get() {
            return if (parent == null) {
                0
            } else {
                val numParents = parent!!.level
                numParents + 1
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