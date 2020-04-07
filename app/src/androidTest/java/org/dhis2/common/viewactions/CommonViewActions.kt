package org.dhis2.common.viewactions

import android.view.View
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.util.TreeIterables
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher

fun openSpinnerPopup(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(Spinner::class.java)
        }

        override fun getDescription(): String {
            return "Opening Spinner"
        }

        override fun perform(uiController: UiController, view: View) {
            val spinner = view as Spinner
            spinner.performClick()
        }
    }
}
fun setSwitchCheckTo(checkValue: Boolean) : ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return "Setting switch check"
        }

        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(SwitchCompat::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            if (view is SwitchCompat) {
                view.isChecked = checkValue
            } else {
                throw IllegalArgumentException("Error the view is not a switch")
            }
        }
    }
}

fun scrollToBottomRecyclerView() : ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return "Recyclerview scrolling until the end"
        }

        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(RecyclerView::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            val recyclerView = view as RecyclerView
            val itemCount = recyclerView.adapter?.itemCount
            val position = itemCount?.minus(1) ?: 0
            recyclerView.scrollToPosition(position)
            uiController?.loopMainThreadUntilIdle()
        }
    }
}

fun clickOnFabChild() : ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return "searching for fab child"
        }

        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(RapidFloatingActionLayout::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            val rootView = (view as RapidFloatingActionLayout)
            for (index in 0 until rootView.childCount) {
                val child = rootView.getChildAt(index)
                child.performClick()
                uiController?.loopMainThreadUntilIdle()
                return
            }
        }
    }
}

fun waitForTransitionUntil(idView:Int) : ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return "Checking tabLayoutTransition"
        }

        override fun getConstraints(): Matcher<View> {
            return isRoot()
        }

        override fun perform(uiController: UiController?, view: View?) {
            var loops = 0
            while (isTransitionInProgress(view) && loops < 200) {
                loops++
                uiController?.loopMainThreadForAtLeast(100)
            }
        }

        private fun isTransitionInProgress(view: View?): Boolean {
            var viewElementCount = 0
            val matcher = allOf<View>(isCompletelyDisplayed(), withId(idView))
            for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                if (matcher.matches(child)) {
                    viewElementCount++
                }
            }
            return viewElementCount > 1
        }
    }
}