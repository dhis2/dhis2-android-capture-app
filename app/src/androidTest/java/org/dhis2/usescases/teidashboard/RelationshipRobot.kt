package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import kotlinx.android.synthetic.main.fragment_relationships.view.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionViewHolder
import org.hamcrest.CoreMatchers.allOf

fun relationshipRobot(relationshipRobot: RelationshipRobot.() -> Unit) {
    RelationshipRobot().apply {
        relationshipRobot()
    }
}

class RelationshipRobot: BaseRobot() {
    fun clickOnFabAdd() {
        onView(withId(R.id.rfab)).perform(click())
    }

    fun clickOnMotherRelationship() {
       // onView(withId(R.id.clear_relationship_button))
        /*onView(allOf(withId(R.id.button1), withParent(withId(R.id.include_one))))
                .check(matches(isDisplayed()))
                .perform(click())*/
        //onView(withId(R.id.rfabLayout)).check(matches(withText("Mother-Child__a-to-b__(Person-Person)")))

        onView(allOf(withText("Mother-Child_a-to-b_(Person-Person)"), withParent(withId(R.id.rfabLayout))))
                .check(matches(isDisplayed()))
               // .perform(click());
    }

    fun clickOnClearRelationship() {
        // not sure how to clear relationships
        onView(withId(R.id.relationship_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition<ProgramStageSelectionViewHolder>(0, click()))
        onView(withId(R.id.clear_relationship_button))
    }
}