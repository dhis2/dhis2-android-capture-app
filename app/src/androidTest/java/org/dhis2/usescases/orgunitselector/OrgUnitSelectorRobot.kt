package org.dhis2.usescases.orgunitselector

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.dhis2.common.BaseRobot
import org.dhis2.ui.dialogs.orgunit.DONE_TEST_TAG
import org.dhis2.ui.dialogs.orgunit.ITEM_CHECK_TEST_TAG

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
        composeTestRule.onNodeWithTag("$ITEM_CHECK_TEST_TAG$orgUnitName")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag(DONE_TEST_TAG).performClick()
    }
}