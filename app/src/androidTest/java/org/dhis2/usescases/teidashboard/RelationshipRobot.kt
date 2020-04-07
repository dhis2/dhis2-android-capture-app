package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
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

    fun clickOnRelationshipType() {
       // onView(withText("Mother-Child_a-to-b_(Person-Person)")).perform(click())
       // onView(allOf(withParent(withId(R.id.rfab)), findChildFabButton(1)))
        //onView(withTagValue(is((Object) tagValue)));
        onView(allOf(withId(R.id.rfab__content_label_list_root_view),
                hasDescendant(withText("Mother-Child_a-to-b_(Person-Person)")))).perform()
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