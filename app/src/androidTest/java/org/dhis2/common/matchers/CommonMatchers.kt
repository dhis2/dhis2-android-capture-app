package org.dhis2.common.matchers

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import androidx.annotation.NonNull
import androidx.test.espresso.Root
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


fun withErrorEnabledTil(): Matcher<View> {
    return object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("error enabled ")
        }

        override fun matchesSafely(item: TextInputLayout): Boolean {
            return item.isErrorEnabled
        }
    }
}

fun withErrorMessageShownTil(): Matcher<View> {
    return object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("message shown ")
        }

        override  fun matchesSafely(item: TextInputLayout): Boolean {
            return item.error!!.isNotEmpty()
        }
    }
}

fun clickOnTab(tabIndex: Int): Matcher<View> {
    return nthChildOf(withClassName(containsString("SlidingTab")), tabIndex)
}

fun nthChildOf(parentMatcher: Matcher<View>,
               childPosition: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("with $childPosition child is view of type SlidingTab")
        }

        public override fun matchesSafely(view: View): Boolean {
            if (view.parent !is ViewGroup) {
                return parentMatcher.matches(view.parent)
            }
            val group = view.parent as ViewGroup
            return parentMatcher.matches(view.parent) && group.getChildAt(childPosition) == view
        }
    }
}

@NonNull
fun adaptedDataNotEmpty(): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun matchesSafely(view: View): Boolean {
            if (view !is AdapterView<*>) {
                return false
            }
            return view.adapter.count > 0
        }

        override fun describeTo(description: Description) {
            description.appendText("adaptedDataNotEmpty ")
        }
    }
}

fun isToast(): Matcher<Root> {
    return object : TypeSafeMatcher<Root>() {
        override fun matchesSafely(view: Root): Boolean {
            val type = view.windowLayoutParams.get().type
            if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                val windowToken = view.decorView.windowToken
                val appToken = view.decorView.applicationWindowToken
                if (windowToken === appToken) {
                    return true
                }
            }
            return false
        }

        override fun describeTo(description: Description) {
            description.appendText("is toast")
        }
    }
}