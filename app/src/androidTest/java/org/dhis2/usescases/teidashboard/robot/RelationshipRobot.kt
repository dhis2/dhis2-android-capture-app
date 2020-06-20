package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
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
        onView(
            allOf(
                withId(R.id.rfab__content_label_list_root_view),
                hasDescendant(withText("Mother-Child_a-to-b_(Person-Person)"))
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
                            hasDescendant(withText("Mother-Child_a-to-b_(Person-Person)")),
                            hasDescendant(withText(tei))
                        )
                    )
                )
            ))
    }
}
