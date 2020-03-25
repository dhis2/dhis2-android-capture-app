package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.notes.noteDetail.NoteDetailActivity
import org.dhis2.utils.Constants
import org.hamcrest.CoreMatchers.allOf

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
        onView(withId(R.id.noteText)).perform(TypeTextAction("fkjadshfkjhdsakjfsa"))
        closeKeyboard()
    }

    fun clickOnSaveButton() {
        onView(withId(R.id.saveButton))
                .perform(click())
        Thread.sleep(1000)
    }

    fun checkNewNoteWasCreated() {
        onView(withId(R.id.notes_recycler))
                .check(matches(isDisplayed()))
                //.check(matches(allOf(isDisplayed(), isNotEmpty())))

        /*Intents.intended(allOf(hasExtra(Constants.NOTE_ID, "dafad"),
                hasComponent(NoteDetailActivity::class.java.name)))*/
    }

    fun clickOnClearButton() {
        onView(withId(R.id.clearButton)).perform(click())
    }
}