package org.dhis2.usescases.flow.syncFlow.robot

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import org.dhis2.common.BaseRobot

fun eventWithoutRegistrationRobot(
    composeTestRule: ComposeTestRule,
    eventWithoutRegistrationRobot: EventWithoutRegistrationRobot.() -> Unit
) {
    EventWithoutRegistrationRobot(composeTestRule).apply {
        eventWithoutRegistrationRobot()
    }
}

class EventWithoutRegistrationRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun clickOnEventAtPosition(position: Int) {
        composeTestRule
            .onAllNodesWithTag("EVENT_ITEM")[position]
            .performClick()
    }
}