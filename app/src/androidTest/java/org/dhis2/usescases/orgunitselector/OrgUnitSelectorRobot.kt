@file:OptIn(ExperimentalTestApi::class)

package org.dhis2.usescases.orgunitselector

import androidx.compose.ui.semantics.SemanticsProperties.TestTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.common.BaseRobot

fun orgUnitSelectorRobot(
    composeTestRule: ComposeTestRule,
    robotBody: OrgUnitSelectorRobot.() -> Unit,
) {
    OrgUnitSelectorRobot(composeTestRule).apply {
        robotBody()
    }
}

class OrgUnitSelectorRobot(private val composeTestRule: ComposeTestRule) : BaseRobot() {
    fun selectTreeOrgUnit(orgUnitName: String) {
        val doneText =
            InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.done)
        composeTestRule.onNodeWithTag("ORG_TREE_ITEM_$orgUnitName")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(doneText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickFirstOrgUnitCheckbox() {
        composeTestRule.onAllNodes(
            SemanticsMatcher("tag starts with ORG_TREE_ITEM_CHECKBOX_") {
                runCatching { it.config[TestTag] }.getOrNull()?.startsWith("ORG_TREE_ITEM_CHECKBOX_") == true
            },
        )[0].performClick()
        composeTestRule.waitForIdle()
    }

    fun clickDone() {
        val doneText =
            InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.done)
        composeTestRule.onNodeWithText(doneText)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
    }
}
