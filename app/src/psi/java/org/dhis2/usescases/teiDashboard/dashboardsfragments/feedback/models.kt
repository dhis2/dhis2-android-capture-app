package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import org.dhis2.R
import tellh.com.recyclertreeview_lib.LayoutItemType

enum class ProgramType { HNQIS, RDQA }

data class FeedbackProgram(val uid: String, val programType: ProgramType)

class FeedbackItemValue(val data: String?, val color: String)

class FeedbackItem(val name: String, val value: FeedbackItemValue? = null) : LayoutItemType {
    override fun getLayoutId(): Int {
        return R.layout.item_feedback
    }
}

class FeedbackHelpItem(val text: String) : LayoutItemType {
    override fun getLayoutId(): Int {
        return R.layout.item_help_feedback
    }
}

/*
class FeedbackNode<T>(value:T){
    var value:T = value
    var parent:FeedbackNode<T>? = null

    var children:MutableList<FeedbackNode<T>> = mutableListOf()

    fun addChild(node:FeedbackNode<T>){
        children.add(node)
        node.parent = this
    }
    override fun toString(): String {
        var s = "$value"
        if (children.isNotEmpty()) {
            s += " {" + children.map { it.toString() } + " }"
        }
        return s
    }
}*/
