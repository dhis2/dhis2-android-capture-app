package org.dhis2.common

import android.app.Activity
import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.view.View
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.TreeIterables
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import org.dhis2.R
import org.dhis2.usescases.main.MainActivity
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import java.lang.Thread.sleep

open class BaseRobot {

    fun pressBack(): BaseRobot {
        UiDevice.getInstance(getInstrumentation()).pressBack()
        return this
    }

    fun acceptGenericDialog() {
        onView(withId(android.R.id.button1)).perform(ViewActions.click())
    }

    fun closeKeyboard() {
        onView(isRoot()).perform(closeSoftKeyboard())
    }

    fun pressImeActionButton(@IdRes editTextId: Int? = null) {
        if (editTextId != null) {
            onView(withId(editTextId)).perform(ViewActions.pressImeActionButton())
        } else {
            onView(
                allOf(
                    isAssignableFrom(EditText::class.java),
                    hasFocus()
                )
            ).perform(ViewActions.pressImeActionButton())
        }
    }

    fun waitToDebounce(millis: Long) {
        Thread.sleep(millis)
    }

    inline fun <reified T : Activity> waitUntilActivityVisible() {
        val startTime = System.currentTimeMillis()
        while (!isVisible<T>()) {
            Thread.sleep(CONDITION_CHECK_INTERVAL)
            if (System.currentTimeMillis() - startTime >= TIMEOUT) {
                throw AssertionError(
                    "Activity ${T::class.java.simpleName} " +
                        "not visible after $TIMEOUT milliseconds"
                )
            }
        }
    }

    /**
     * Perform action of implicitly waiting for a certain view.
     * This differs from EspressoExtensions.searchFor in that,
     * upon failure to locate an element, it will fetch a new root view
     * in which to traverse searching for our @param match
     *
     * @param viewMatcher ViewMatcher used to find our view
     */
    fun waitForView(
        viewMatcher: Matcher<View>,
        waitMillis: Int = 5000,
        waitMillisPerTry: Long = 100
    ): ViewInteraction {

        // Derive the max tries
        val maxTries = waitMillis / waitMillisPerTry.toInt()

        var tries = 0

        for (i in 0..maxTries)
            try {
                // Track the amount of times we've tried
                tries++

                // Search the root for the view
                onView(isRoot()).perform(searchFor(viewMatcher))

                // If we're here, we found our view. Now return it
                return onView(viewMatcher)

            } catch (e: Exception) {

                if (tries == maxTries) {
                    throw e
                }
                sleep(waitMillisPerTry)
            }

        throw Exception("Error finding a view matching $viewMatcher")
    }

    /**
     * Perform action of waiting for a certain view within a single root view
     * @param matcher Generic Matcher used to find our view
     */
    fun searchFor(matcher: Matcher<View>): ViewAction {

        return object : ViewAction {

            override fun getConstraints(): Matcher<View> {
                return isRoot()
            }

            override fun getDescription(): String {
                return "searching for view $matcher in the root view"
            }

            override fun perform(uiController: UiController, view: View) {

                var tries = 0
                val childViews: Iterable<View> = TreeIterables.breadthFirstViewTraversal(view)

                // Look for the match in the tree of childviews
                childViews.forEach {
                    tries++
                    if (matcher.matches(it)) {
                        // found the view
                        return
                    }
                }

                throw NoMatchingViewException.Builder()
                    .withRootView(view)
                    .withViewMatcher(matcher)
                    .build()
            }
        }
    }

    inline fun <reified T : Activity> isVisible(): Boolean {
        val am = InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(
            ACTIVITY_SERVICE
        ) as ActivityManager
        val visibleActivityName = am.appTasks[0].taskInfo.baseActivity!!.className
        return visibleActivityName == T::class.java.name
    }

     fun <T : Activity> checkActivityIsFinishing(rule : ActivityTestRule<T>){
        assert(rule.activity.isFinishing)
    }

    companion object {
        const val TIMEOUT = 5000L
        const val CONDITION_CHECK_INTERVAL = 200L
    }
}
