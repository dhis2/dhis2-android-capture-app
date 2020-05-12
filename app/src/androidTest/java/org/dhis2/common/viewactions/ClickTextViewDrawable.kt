package org.dhis2.common.viewactions

import android.graphics.Point
import android.graphics.Rect
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

class ClickDrawableAction(@param:Location @field:Location private val drawableLocation: Int) : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return allOf(isAssignableFrom(TextView::class.java), object : BoundedMatcher<View, TextView>(TextView::class.java) {
            override fun matchesSafely(tv: TextView): Boolean {
                return tv.requestFocusFromTouch() && tv.compoundDrawables[drawableLocation] != null
            }

            override fun describeTo(description: Description) {
                description.appendText(DESCRIPTION_HAS_DRAWABLE)
            }
        })
    }

    override fun getDescription(): String {
        return DESCRIPTION_CLICK_DRAWABLE
    }

    override fun perform(uiController: UiController, view: View) {
        val tv = view as TextView
        if (tv.requestFocusFromTouch()) {
            val drawableBounds = tv.compoundDrawables[drawableLocation].bounds
            val clickPoint = arrayOfNulls<Point>(SIZE_CLICK_POINT)
            calculateDrawableLocation(clickPoint, tv, drawableBounds)

        }
    }

    private fun calculateDrawableLocation(clickPoint: Array<Point?>, tv: TextView, drawableBounds: Rect) {
        clickPoint[LEFT] = Point(tv.left + drawableBounds.width() / HALF_DIVISOR,
                (tv.pivotY + drawableBounds.height() / HALF_DIVISOR).toInt())
        clickPoint[TOP] = Point((tv.pivotX + drawableBounds.width() / HALF_DIVISOR).toInt(),
                tv.top + drawableBounds.height() / HALF_DIVISOR)
        clickPoint[RIGHT] = Point(tv.right + drawableBounds.width() / HALF_DIVISOR,
                (tv.pivotY + drawableBounds.height() / HALF_DIVISOR).toInt())
        clickPoint[BOTTOM] = Point((tv.pivotX + drawableBounds.width() / HALF_DIVISOR).toInt(),
                tv.bottom + drawableBounds.height() / HALF_DIVISOR)
        clickPoint[drawableLocation]?.let { point ->
            if (tv.dispatchTouchEvent(MotionEvent.obtain(
                                    SystemClock.uptimeMillis(),
                                    SystemClock.uptimeMillis(),
                                    MotionEvent.ACTION_DOWN,
                                    point.x.toFloat(),
                                    point.y.toFloat(),
                                    0))) {
                tv.dispatchTouchEvent(MotionEvent.obtain(
                                SystemClock.uptimeMillis(),
                                SystemClock.uptimeMillis(),
                                MotionEvent.ACTION_UP,
                                point.x.toFloat(),
                                point.y.toFloat(),
                                0))
            }
        }
    }

    @IntDef(LEFT, TOP, RIGHT, BOTTOM)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Location

    companion object {
        const val LEFT = 0
        const val TOP = 1
        const val RIGHT = 2
        const val BOTTOM = 3
        const val SIZE_CLICK_POINT = 4
        const val HALF_DIVISOR = 2
        const val DESCRIPTION_HAS_DRAWABLE = "has drawable"
        const val DESCRIPTION_CLICK_DRAWABLE = "click drawable "
    }
}