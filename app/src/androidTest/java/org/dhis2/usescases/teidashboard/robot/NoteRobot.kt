package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.dhis2.usescases.notes.noteDetail.NoteDetailActivity
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

    fun verifyNoteDetailActivityIsLaunched() {
        Intents.intended(allOf(hasComponent(NoteDetailActivity::class.java.name)))
    }

    fun typeNote(text: String) {
        onView(withId(R.id.noteText)).perform(TypeTextAction(text))
        closeKeyboard()
    }

    fun clickOnSaveButton() {
        waitForView(withText(R.string.save))
            .check(matches(allOf(isDisplayed(), isEnabled())))
            .perform(click())
    }

    fun clickYesOnAlertDialog() {
        waitForView(withText(R.string.yes))
            .check(matches(isDisplayed()))
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
        waitForView(withId(R.id.notes_recycler)).check(
            matches(
                allOf(
                    isDisplayed(),
                    isNotEmpty(),
                    atPosition(0, hasDescendant(withText(text)))
                )
            )
        )
    }

    fun clickOnClearButton() {
        waitForView(withText(R.string.clear))
            .check(matches(allOf(isDisplayed(), isEnabled())))
            .perform(click())
    }

    fun checkNoteDetails(user: String, noteText: String) {
        waitForView(withId(R.id.notes_recycler)).check(matches(isDisplayed()))
        waitForView(allOf(withId(R.id.storeBy), withEffectiveVisibility(Visibility.VISIBLE), withText(user)))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        waitForView(allOf(withId(R.id.note_text), withEffectiveVisibility(Visibility.VISIBLE), withText(noteText)))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
}
