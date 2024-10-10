package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import org.dhis2.common.BaseRobot
import org.dhis2.utils.dialFloatingActionButton.FAB_ID
import org.hamcrest.CoreMatchers.equalTo

fun relationshipRobot(relationshipRobot: RelationshipRobot.() -> Unit) {
    RelationshipRobot().apply {
        relationshipRobot()
    }
}

class RelationshipRobot : BaseRobot() {
    fun clickOnFabAdd() {
        onView(withId(FAB_ID)).perform(click())
    }

    fun clickOnRelationshipType() {
        onView(
            withTagValue(
                equalTo(relationshipType)
            )
        ).perform(click())
    }

    companion object {
        const val relationshipType = "Mother-Child_a-to-b_(Person-Person)"
    }
}
