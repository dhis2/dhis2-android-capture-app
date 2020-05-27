package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.hamcrest.CoreMatchers.allOf

fun relationshipRobot(relationshipRobot: RelationshipRobot.() -> Unit) {
    RelationshipRobot().apply {
        relationshipRobot()
    }
}

class RelationshipRobot : BaseRobot() {
    fun clickOnFabAdd() {
        onView(withId(R.id.rfab)).perform(click())
    }

    fun clickOnRelationshipType() {
        // onView(withText("Mother-Child_a-to-b_(Person-Person)")).perform(click())
        // onView(allOf(withParent(withId(R.id.rfab)), findChildFabButton(1)))
        // onView(withTagValue(is((Object) tagValue)));
        onView(
            allOf(
                withId(R.id.rfab__content_label_list_root_view),
                hasDescendant(withText("Mother-Child_a-to-b_(Person-Person)"))
            )
        ).perform(click())
    }

    fun clickOnTEI() {
    }

    fun checkRelationshipWasCreated() {
    }
}
