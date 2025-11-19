package org.dhis2.common

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.util.TreeIterables
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.UiDevice
import org.hamcrest.Matcher
import java.lang.Thread.sleep

open class BaseRobot {

    fun pressBack(): BaseRobot {
        UiDevice.getInstance(getInstrumentation()).pressBack()
        return this
    }

    fun closeKeyboard() {
        try {
            val instrumentation = getInstrumentation()
            instrumentation.runOnMainSync {
                // Try to find a resumed Activity to obtain a window token
                val activity = try {
                    ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(
                            Stage.RESUMED
                        )
                        .firstOrNull()
                } catch (_: Throwable) {
                    null
                }

                val context = activity ?: return@runOnMainSync
                val view = context.currentFocus ?: context.window?.decorView
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                if (view != null && imm != null) {
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        } catch (_: Exception) {
            // Ignore keyboard close failures during cleanup
            // This can happen if the activity is already finishing or doesn't have focus
        }
    }

    fun itemWithTextIsDisplayed(
        text: String,
        substring: Boolean,
        composeTestRule: ComposeContentTestRule
    ) {
        composeTestRule.onNodeWithText(text, substring)
            .assertIsDisplayed()
    }

    fun waitToDebounce(millis: Long) {
        sleep(millis)
    }

    fun getString(stringId: Int): String = getInstrumentation().targetContext.getString(stringId)

    inline fun <reified T : Activity> waitUntilActivityVisible() {
        val startTime = System.currentTimeMillis()
        while (!isVisible<T>()) {
            sleep(CONDITION_CHECK_INTERVAL)
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
        waitMillis: Int = TIMEOUT.toInt(),
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
        val am = getInstrumentation().targetContext.getSystemService(
            ACTIVITY_SERVICE
        ) as ActivityManager
        val visibleActivityName = am.appTasks[0].taskInfo.baseActivity!!.className
        return visibleActivityName == T::class.java.name
    }

    companion object {
        const val TIMEOUT = 5000L
        const val CONDITION_CHECK_INTERVAL = 200L
    }
}
