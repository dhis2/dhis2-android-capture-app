package org.dhis2.common.matchers

import android.view.View
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class RecyclerviewMatchers {

    companion object {
        fun isNotEmpty(): Matcher<View> {
            return object : TypeSafeMatcher<View>() {
                public override fun matchesSafely(view: View): Boolean {
                    return (view as RecyclerView).childCount > 0
                }

                override fun describeTo(description: Description) {
                    description.appendText("RecyclerView should not be empty")
                }
            }
        }

        fun withSize(size: Int): Matcher<View> {
            return object : TypeSafeMatcher<View>() {
                public override fun matchesSafely(view: View): Boolean {
                    val recyclerview = view as RecyclerView
                    return recyclerview.childCount == size
                }

                override fun describeTo(description: Description) {
                    description.appendText("Recyclerview should have total elements of $size")
                }
            }
        }

        fun atPosition(position: Int,
                       @NonNull itemMatcher: Matcher<View>): Matcher<View> {
            return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText("has item in recyclerview  at position $position")
                    itemMatcher.describeTo(description)
                }

                override fun matchesSafely(view: RecyclerView): Boolean {
                    val viewHolder = view.findViewHolderForAdapterPosition(position) ?: return false
                    return itemMatcher.matches(viewHolder.itemView)
                }
            }
        }
    }
}