package org.dhis2.common.matchers

import android.view.View
import android.widget.DatePicker
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

class DatePickerMatchers {
    companion object {
        fun matchesDate(year:Int, monthOfYear:Int, dayOfMonth:Int): Matcher<View> {
            return object : BoundedMatcher<View, DatePicker>(DatePicker::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText("matches date:")
                }
                override fun matchesSafely(item: DatePicker): Boolean {
                    return year === item.year && monthOfYear === item.month+1 && dayOfMonth === item.dayOfMonth
                }
            }
        }
    }
}