package org.dhis2.core.ui.viewpager2

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2

// from https://issuetracker.google.com/issues/123006042#comment21
/**
 * Layout to wrap a scrollable component inside a ViewPager2. Provided as a solution to the problem
 * where pages of ViewPager2 have nested scrollable elements that scroll in the same direction as
 * ViewPager2. The scrollable element needs to be the immediate and only child of this host layout.
 *
 * This solution has limitations when using multiple levels of nested scrollable elements
 * (e.g. a horizontal RecyclerView in a vertical RecyclerView in a horizontal ViewPager2).
 */
class NestedScrollableHost : FrameLayout {
    private var touchSlop = 0
    private var initialX = 0.0f
    private var initialY = 0.0f
    private fun parentViewPager(): ViewPager2? {
        var v = this.parent as View
        while (v != null && v !is ViewPager2) v = v.parent as View
        return v as ViewPager2
    }

    private fun child(): View? {
        return if (this.childCount > 0) getChildAt(0) else null
    }

    private fun init() {
        touchSlop = ViewConfiguration.get(this.context).scaledTouchSlop
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun canChildScroll(orientation: Int, delta: Float): Boolean {
        val direction = Math.signum(-delta).toInt()
        val child = child() ?: return false
        if (orientation == 0) return child.canScrollHorizontally(direction)
        return if (orientation == 1) child.canScrollVertically(direction) else false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        handleInterceptTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    private fun handleInterceptTouchEvent(ev: MotionEvent) {
        val vp = parentViewPager() ?: return
        val orientation = vp.orientation

        // Early return if child can't scroll in same direction as parent
        if (!canChildScroll(orientation, -1.0f) && !canChildScroll(orientation, 1.0f)) return
        if (ev.action == MotionEvent.ACTION_DOWN) {
            initialX = ev.x
            initialY = ev.y
            this.parent.requestDisallowInterceptTouchEvent(true)
        } else if (ev.action == MotionEvent.ACTION_MOVE) {
            val dx = ev.x - initialX
            val dy = ev.y - initialY
            val isVpHorizontal = orientation == ViewPager2.ORIENTATION_HORIZONTAL

            // assuming ViewPager2 touch-slop is 2x touch-slop of child
            val scaleDx = Math.abs(dx) * if (isVpHorizontal) 0.5f else 1.0f
            val scaleDy = Math.abs(dy) * if (isVpHorizontal) 1.0f else 0.5f
            if (scaleDx > touchSlop || scaleDy > touchSlop) {
                if (isVpHorizontal == scaleDy > scaleDx) {
                    // Gesture is perpendicular, allow all parents to intercept
                    this.parent.requestDisallowInterceptTouchEvent(false)
                } else {
                    // Gesture is parallel, query child if movement in that direction is possible
                    if (canChildScroll(orientation, if (isVpHorizontal) dx else dy)) {
                        this.parent.requestDisallowInterceptTouchEvent(true)
                    } else {
                        // Child cannot scroll, allow all parents to intercept
                        this.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
            }
        }
    }
}