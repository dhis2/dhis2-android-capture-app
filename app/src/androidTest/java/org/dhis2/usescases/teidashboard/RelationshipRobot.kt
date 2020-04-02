package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import kotlinx.android.synthetic.main.fragment_relationships.view.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionViewHolder

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
        onView(withId(R.id.clear_relationship_button))
    }

    fun clickOnClearRelationship() {
        // not sure how to clear relationships
        onView(withId(R.id.relationship_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition<ProgramStageSelectionViewHolder>(0, click()))
        onView(withId(R.id.clear_relationship_button))
    }
}