package org.dhis2.usescases.deleteAccount

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot

fun deleteAccountRobot(deleteAccountBody: DeleteAccountRobot.() -> Unit) {
    DeleteAccountRobot().apply {
        deleteAccountBody()
    }
}

class DeleteAccountRobot : BaseRobot() {
}
