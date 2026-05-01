package org.dhis2.usescases.teidashboard.robot

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
        waitForView(withId(R.id.addNoteButton)).check(matches(isDisplayed())).perform(click())
    }

    fun verifyNoteDetailActivityIsLaunched() {
        Intents.intended(allOf(hasComponent(NoteDetailActivity::class.java.name)))
    }

    fun typeNote(text: String) {
        waitForView(withId(R.id.noteText)).perform(TypeTextAction(text))
        closeKeyboard()
    }

    fun clickOnSaveButton() {
        waitForView(allOf(withId(R.id.saveButton), withText(R.string.save)))
            .check(matches(allOf(isDisplayed(), isEnabled())))
            .perform(click())
    }

    fun clickYesOnAlertDialog() {
        waitForView(withId(android.R.id.button1), waitMillis = DIALOG_WAIT_TIMEOUT_MS)
            .check(matches(isDisplayed()))
            .perform(click())
    }

    fun checkNoteWasNotCreated(text: String) {
        waitForView(withId(R.id.notes_recycler), waitMillis = NOTES_WAIT_TIMEOUT_MS).check(
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
        waitForView(
            allOf(
                withId(R.id.notes_recycler),
                isDisplayed(),
                isNotEmpty(),
                atPosition(0, hasDescendant(withText(text)))
            ),
            waitMillis = NOTES_WAIT_TIMEOUT_MS
        )
    }

    fun clickOnClearButton() {
        waitForView(withText(R.string.clear))
            .check(matches(allOf(isDisplayed(), isEnabled())))
            .perform(click())
    }

    fun checkNoteDetails(user: String, noteText: String) {
        waitForView(withId(R.id.notes_recycler), waitMillis = NOTES_WAIT_TIMEOUT_MS)
            .check(matches(isDisplayed()))
        waitForView(
            allOf(
                withId(R.id.storeBy),
                withEffectiveVisibility(Visibility.VISIBLE),
                withText(user)
            )
        )
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        waitForView(
            allOf(
                withId(R.id.note_text),
                withEffectiveVisibility(Visibility.VISIBLE),
                withText(noteText)
            )
        )
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    companion object {
        private const val DIALOG_WAIT_TIMEOUT_MS = 10000
        private const val NOTES_WAIT_TIMEOUT_MS = 15000
    }
}
