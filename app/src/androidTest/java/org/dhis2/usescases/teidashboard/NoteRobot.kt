package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot

fun noteRobot(noteRobot: NoteRobot.() -> Unit) {
    NoteRobot().apply {
        noteRobot()
    }
}

class NoteRobot: BaseRobot() {

    fun clickOnFabAddNewNote() {
        onView(withId(R.id.addNoteButton)).perform(click())
    }

    fun checkFabDisplay() {
        onView(withId(R.id.addNoteButton)).check(matches(isDisplayed()))
    }

    fun typeNote() {
        onView(withId(R.id.noteText)).perform(click(), TypeTextAction("fkjadshfkjhdsakjfsa"))
    }

    fun clickOnSaveButton() {
        onView(withId(R.id.saveButton)).perform(click())
    }

    fun checkNewNoteWasCreated() {

    }

    fun clickOnClearButton() {
        onView(withId(R.id.clearButton)).perform(click())
    }
}