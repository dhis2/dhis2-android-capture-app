package org.dhis2.usescases.main

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.main.program.HOME_ITEMS
import org.dhis2.usescases.main.program.hasPrograms

private const val HOME_NAVIGATION_TIMEOUT = 15000L

fun homeRobot(
    composeTestRule: ComposeTestRule,
    robotBody: MainRobot.() -> Unit
) {
    MainRobot(composeTestRule).apply {
        robotBody()
    }
}

class MainRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    @OptIn(ExperimentalTestApi::class)
    fun checkViewIsNotEmpty(composeTestRule: ComposeTestRule) {
        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(HOME_ITEMS) and SemanticsMatcher.expectValue(hasPrograms, true),
            HOME_NAVIGATION_TIMEOUT,
        )
        composeTestRule.onNodeWithTag(HOME_ITEMS).assert(
            SemanticsMatcher.expectValue(hasPrograms, true)
        )
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkHomeIsDisplayed(composeTestRule: ComposeTestRule) {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(HOME_ITEMS), HOME_NAVIGATION_TIMEOUT)
        composeTestRule.onNodeWithTag(HOME_ITEMS).assertIsDisplayed()
    }
}
