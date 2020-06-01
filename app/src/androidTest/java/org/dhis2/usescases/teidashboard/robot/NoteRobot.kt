package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.dhis2.usescases.notes.NotesViewHolder
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not

fun noteRobot(noteRobot: NoteRobot.() -> Unit) {
    NoteRobot().apply {
        noteRobot()
    }
}

class NoteRobot : BaseRobot() {

    fun clickOnFabAddNewNote() {
        onView(withId(R.id.addNoteButton)).check(matches(isDisplayed())).perform(click())
    }

    fun clickOnNoteWithPosition(position: Int) {
        onView(withId(R.id.notes_recycler))
            .perform(actionOnItemAtPosition<NotesViewHolder>(position, click()))
    }

    fun typeNote(text: String) {
        onView(withId(R.id.noteText)).perform(TypeTextAction(text))
        closeKeyboard()
    }

    fun clickOnSaveButton() {
        onView(withId(R.id.saveButton))
            .perform(click())
    }

    fun clickYesOnAlertDialog() {
        onView(withText(R.string.yes))
            .perform(click())
    }

    fun checkNoteWasNotCreated(text: String) {
        onView(withId(R.id.notes_recycler)).check(
            matches(
                not(
                    atPosition(
                        0,
                        hasDescendant((withText(text)))
                    )
                )
            )
        )
    }

    fun checkNewNoteWasCreated(text: String) {
        onView(withId(R.id.notes_recycler)).check(
            matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(0, hasDescendant(withText(text)))
                )
            )
        )
    }

    fun clickOnClearButton() {
        onView(withId(R.id.clearButton)).perform(click())
    }

    fun checkNoteDetails(user: String, noteText: String) {
        onView(withId(R.id.storeBy)).check(matches(withText(user)))
        onView(withId(R.id.note)).check(matches(withText(noteText)))
    }
}
