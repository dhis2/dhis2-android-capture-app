package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.dhis2.utils.dialFloatingActionButton.FAB_ID
import org.hamcrest.CoreMatchers.allOf
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

    fun checkRelationshipWasCreated(position: Int, tei: String) {
        onView(withId(R.id.relationship_recycler))
            .check(matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(
                        position, allOf(
                            hasDescendant(withText(relationshipType)),
                            hasDescendant(withText(tei))
                        )
                    )
                )
            ))
    }

    companion object {
        const val relationshipType = "Mother-Child_a-to-b_(Person-Person)"
    }
}
