package org.dhis2.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView

class FixedBottomViewBehavior: CoordinatorLayout.Behavior<View> {

    constructor(): super()
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is NestedScrollView
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        if (ViewCompat.isLaidOut(parent)) {
            //attach our bottom view to the bottom of CoordinatorLayout
            val bottomMargin = (child.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            var xDelta = 0f
            if (dependency.top > parent.bottom.toFloat() - child.height.toFloat() - 2f * bottomMargin - 150f) {
                child.y = dependency.top + bottomMargin.toFloat()
                xDelta = (dependency.top - parent.bottom).toFloat() + child.width.toFloat() + 2f * bottomMargin + 150f
            } else {
                child.y = parent.bottom.toFloat() - child.height.toFloat() - bottomMargin.toFloat() - 150f
            }

            child.x = parent.right - child.width - bottomMargin + xDelta


        }
        return false
    }
}