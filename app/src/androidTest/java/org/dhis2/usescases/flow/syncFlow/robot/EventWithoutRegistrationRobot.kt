package org.dhis2.usescases.flow.syncFlow.robot

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewHolder

fun eventWithoutRegistrationRobot(
    composeTestRule: ComposeTestRule,
    eventWithoutRegistrationRobot: EventWithoutRegistrationRobot.() -> Unit
) {
    EventWithoutRegistrationRobot(composeTestRule).apply {
        eventWithoutRegistrationRobot()
    }
}

class EventWithoutRegistrationRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    @OptIn(ExperimentalTestApi::class)
    fun clickOnEventAtPosition(position: Int) {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("EVENT_ITEM"))
        composeTestRule
            .onAllNodesWithTag("EVENT_ITEM", true)[position]
            .performClick()
    }
}