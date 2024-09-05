package org.dhis2.usescases.orgunitselector

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.dhis2.common.BaseRobot

fun orgUnitSelectorRobot(
    composeTestRule: ComposeTestRule,
    robotBody: OrgUnitSelectorRobot.() -> Unit
) {
    OrgUnitSelectorRobot(composeTestRule).apply {
        robotBody()
    }
}

class OrgUnitSelectorRobot(private val composeTestRule: ComposeTestRule) : BaseRobot() {
    fun selectTreeOrgUnit(orgUnitName: String) {
        composeTestRule.onNodeWithTag("ORG_TREE_ITEM_$orgUnitName")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").performClick()
    }
}