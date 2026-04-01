package org.dhis2.usescases.searchte.robot

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasNoMoreResultsInProgram
import org.dhis2.common.viewactions.openSpinnerPopup
import org.dhis2.usescases.searchTrackEntity.listView.SearchResult
import org.dhis2.usescases.searchte.entity.DisplayListFieldsUIModel
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import kotlin.test.assertContains

fun searchTeiRobot(
    composeTestRule: ComposeTestRule,
    searchTeiRobot: SearchTeiRobot.() -> Unit
) {
    SearchTeiRobot(composeTestRule).apply {
        searchTeiRobot()
    }
}

class SearchTeiRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun clickOnTEI(teiName: String) {
        composeTestRule.onNodeWithText("First name: $teiName", true).performClick()
    }

    fun checkTEIsDelete(teiName: String, teiLastName: String) {
        onView(withId(R.id.scrollView))
            .check(
                matches(
                    not(
                        hasItem(
                            allOf(
                                hasDescendant(withText(teiName)), hasDescendant(
                                    withText(teiLastName)
                                )
                            )
                        )
                    )
                )
            )
    }

    fun openNextSearchParameter(parameterValue: String) {
        composeTestRule.onNodeWithText(parameterValue).performClick()
    }

    fun typeOnNextSearchTextParameter(parameterValue: String) {
        composeTestRule.apply {
            onAllNodesWithTag("INPUT_TEXT_FIELD").onLast().performTextInput(parameterValue)
        }
    }

    fun typeOnDateParameter(dateValue: String) {
        composeTestRule.apply {
            onNodeWithTag("INPUT_DATE_TIME_TEXT_FIELD").performTextInput(dateValue)
        }
    }

    fun clickOnSearch() {
        closeKeyboard()
        composeTestRule.onNodeWithTag("SEARCH_BUTTON").performClick()
        composeTestRule.waitForIdle()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkListOfSearchTEI(title: String, attributes: Map<String?, String>) {
        //Checks title and all attributes are displayed
        composeTestRule.waitUntilAtLeastOneExists(hasText(title))
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        attributes.forEach { item ->
            item.key?.let { composeTestRule.onNodeWithText("$it:", true).assertIsDisplayed() }
            composeTestRule.onNode(
                hasParent(hasTestTag("LIST_CARD_ADDITIONAL_INFO_COLUMN"))
                        and hasText(item.value, true), useUnmergedTree = true
            ).assertIsDisplayed()
        }
    }

    fun checkNoSearchResult() {
        onView(withId(R.id.scrollView))
            .check(
                matches(
                    RecyclerviewMatchers.allElementsWithHolderTypeHave(
                        SearchResult::class.java,
                        allOf(
                            hasNoMoreResultsInProgram()
                        )
                    )
                )
            )
    }

    fun clickOnProgramSpinner() {
        onView(withId(R.id.program_spinner)).perform(openSpinnerPopup())
    }

    fun selectAProgram(program: String) {
        onView(allOf(withId(R.id.spinner_text), withText(program)))
            .perform(click())
    }

    fun checkProgramHasChanged(program: String) {
        onView(withId(R.id.spinner_text)).check(matches(withText(program)))
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkFieldsFromDisplayList(
        displayListFieldsUIModel: DisplayListFieldsUIModel
    ) {
        //Given the title is the first attribute
        val title = "First name: ${displayListFieldsUIModel.name}"
        val displayedAttributes = createAttributesList(displayListFieldsUIModel)
        val showMoreText = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.show_more)
        composeTestRule.waitForIdle()
        composeTestRule.waitUntilAtLeastOneExists(hasText(showMoreText))
        //When we expand all attribute list
        composeTestRule.onNodeWithText(showMoreText, true, useUnmergedTree = true).performClick()
        //Then The title and all attributes are displayed
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        displayedAttributes.forEach { item ->
            item.key?.let { composeTestRule.onNodeWithText("$it:", true).assertIsDisplayed() }
            composeTestRule.onNode(
                hasParent(hasTestTag("LIST_CARD_ADDITIONAL_INFO_COLUMN"))
                        and hasText(item.value, true), useUnmergedTree = true
            ).assertIsDisplayed()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickOnShowMap() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("NAVIGATION_BAR_ITEM_Map"),TIMEOUT)
        composeTestRule.onNodeWithTag("NAVIGATION_BAR_ITEM_Map").performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkCarouselTEICardInfo(firstName: String) {
        // Wait for the carousel to appear
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("MAP_CAROUSEL"), TIMEOUT)
        composeTestRule.onNodeWithTag("MAP_CAROUSEL", true)
            .assertIsDisplayed()
        // Wait for at least one MAP_ITEM to be rendered
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("MAP_ITEM"), TIMEOUT)
        // Wait extra time for data to populate in the cards
        composeTestRule.waitForIdle()
        // Finally assert that the text is displayed
        composeTestRule.onNode(
            hasAnyAncestor(hasTestTag("LIST_CARD_ADDITIONAL_INFO_COLUMN"))
                    and hasText(firstName, ignoreCase = true, substring = true), 
            useUnmergedTree = true
        ).assertIsDisplayed()
    }

    fun clickOnOpenSearch() {
        onView(withId(R.id.openSearchButton)).perform(click())
    }

    fun clickOnEnroll() {
        onView(withId(R.id.createButton)).perform(click())
    }

    fun checkListOfSearchTEIWithAdditionalInfo(title: String, additionalText: String) {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(additionalText).assertIsDisplayed()
    }

    private fun createAttributesList(displayListFieldsUIModel: DisplayListFieldsUIModel) = listOf(
        AdditionalInfoItem(
            key = "Last name",
            value = displayListFieldsUIModel.lastName,
        ),
        AdditionalInfoItem(
            key = "Email",
            value = displayListFieldsUIModel.email,
        ),
        AdditionalInfoItem(
            key = "Date of birth",
            value = displayListFieldsUIModel.birthday,
        ),
        AdditionalInfoItem(
            key = "Address",
            value = displayListFieldsUIModel.address,
        ),
    )

    // --- Methods for the TB Program search flow test ---

    @OptIn(ExperimentalTestApi::class)
    fun checkAddNewTEIButtonIsDisplayedAndEnabled() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("ADD_NEW_BUTTON"), TIMEOUT)
        composeTestRule.onNodeWithTag("ADD_NEW_BUTTON")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkFirstSearchParamIsBarcodeOrQR() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SEARCH_PARAM_ITEM"), TIMEOUT)
        composeTestRule
            .onAllNodesWithTag("SEARCH_PARAM_ITEM")[0]
            .assert(
                hasAnyDescendant(
                    hasContentDescription("QR Code Icon") or
                        hasContentDescription("Barcode Icon"),
                ),
            )
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkFirstSearchParamIsBarcodeOrQROrUnique(expectedLabel: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SEARCH_PARAM_ITEM"), TIMEOUT)
        composeTestRule
            .onAllNodesWithTag("SEARCH_PARAM_ITEM")[0]
            .assert(hasText(expectedLabel))
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkSearchParamCount(expectedCount: Int) {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SEARCH_PARAM_ITEM"), TIMEOUT)
        val count = composeTestRule
            .onAllNodesWithTag("SEARCH_PARAM_ITEM")
            .fetchSemanticsNodes()
            .size
        assert(count == expectedCount) {
            "Expected $expectedCount search parameters, but found $count"
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkSearchButtonIsDisabled() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SEARCH_BUTTON"), TIMEOUT)
        composeTestRule.onNodeWithTag("SEARCH_BUTTON").assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkSearchButtonIsEnabled() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SEARCH_BUTTON"), TIMEOUT)
        composeTestRule.onNodeWithTag("SEARCH_BUTTON").assertIsEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    fun typeOnSearchParameter(label: String, value: String) {
        composeTestRule.waitForIdle()

        composeTestRule.waitUntilAtLeastOneExists(hasText(label), TIMEOUT)
        composeTestRule.onNodeWithText(label).performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("INPUT_TEXT_FIELD"), TIMEOUT)
        composeTestRule.onAllNodesWithTag("INPUT_TEXT_FIELD").onLast().performTextInput(value)
        // Close keyboard after typing to reveal fields that might be hidden behind it
        closeKeyboard()
        composeTestRule.waitForIdle()
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickOnClearSearch() {
        // The reset button in InputText has the test tag "INPUT_TEXT_RESET_BUTTON"
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("INPUT_TEXT_RESET_BUTTON"), TIMEOUT)
        // Click all reset buttons to clear all fields
        val resetButtons = composeTestRule.onAllNodesWithTag("INPUT_TEXT_RESET_BUTTON")
        val count = resetButtons.fetchSemanticsNodes().size
        for (i in 0 until count) {
            // Always click the first one (indices shift as we clear fields)
            composeTestRule.onAllNodesWithTag("INPUT_TEXT_RESET_BUTTON")[0].performClick()
            composeTestRule.waitForIdle()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkFocusedFieldShowsOperatorSupportingText() {
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Exact matches only") or
                hasText("Must match the start of the value") or
                hasText("Must match the end of the value"),
            TIMEOUT,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkMinCharactersErrorIsDisplayed(vararg fieldLabels: String) {
        // Wait for error messages to appear
        composeTestRule.waitForIdle()
        
        // Count the number of "Enter at least" error messages
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Enter at least", substring = true),
            TIMEOUT,
        )
        
        val errorNodes = composeTestRule
            .onAllNodes(hasText("Enter at least", substring = true))
            .fetchSemanticsNodes()
        
        // Verify that we have the expected number of error messages (one per field)
        assert(errorNodes.size >= fieldLabels.size) {
            "Expected at least ${fieldLabels.size} error messages but found ${errorNodes.size}"
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkMinAttributesWarningIsDisplayed() {
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("You need to enter at least", substring = true),
            TIMEOUT,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkSearchResultCount(expectedCount: Int) {
        composeTestRule.waitForIdle()
        val nodes = composeTestRule
            .onAllNodesWithTag("LIST_CARD_ITEM_TAG")
            .fetchSemanticsNodes()
        assert(nodes.size == expectedCount) {
            "Expected $expectedCount search results, but found ${nodes.size}"
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkSearchResultDisplayed(teiName: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasText(teiName, substring = true), TIMEOUT)
        composeTestRule.onNodeWithText(teiName, substring = true).assertIsDisplayed()
    }
}
