package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot

fun noteRobot(noteRobot: NoteRobot.() -> Unit) {
    NoteRobot().apply {
        noteRobot
    }
}

class NoteRobot: BaseRobot() {

    fun clickOnFabAddNewNote() {
        onView(withId(R.id.addNoteButton)).perform(click())
    }

    fun typeNote() {
        onView(withId(R.id.note)).perform(TypeTextAction("fkjadshfkjhdsakjfsa"))
    }

    fun clickOnSaveButton() {
        onView(withId(R.id.saveButton)).perform(click())
    }

    fun checkNewNoteWasCreated() {

    }
}