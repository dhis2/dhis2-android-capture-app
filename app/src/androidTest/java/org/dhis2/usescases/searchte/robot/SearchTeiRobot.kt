package org.dhis2.usescases.searchte.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasNoMoreResultsInProgram
import org.dhis2.common.viewactions.openSpinnerPopup
import org.dhis2.common.viewactions.typeChildViewWithId
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTEViewHolder
import org.dhis2.usescases.searchTrackEntity.listView.SearchResult
import org.dhis2.usescases.searchte.entity.DisplayListFieldsUIModel
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem


fun searchTeiRobot(
    composeTestRule: ComposeTestRule,
    searchTeiRobot: SearchTeiRobot.() -> Unit
) {
    SearchTeiRobot(composeTestRule).apply {
        searchTeiRobot()
    }
}

class SearchTeiRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun clickOnTEI(teiName: String, teiLastName: String) {
        waitForView(
            allOf(
                withId(R.id.scrollView),
                hasDescendant(withText(teiName)),
                hasDescendant(withText(teiLastName))
            )
        ).perform(
            scrollTo<SearchTEViewHolder>(
                allOf(
                    hasDescendant(withText(teiName)),
                    hasDescendant(withText(teiLastName))
                )
            ),
            actionOnItem<SearchTEViewHolder>(
                allOf(
                    hasDescendant(withText(teiName)),
                    hasDescendant(withText(teiLastName))
                ), click()
            )
        )
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

    fun typeAttributeAtPosition(searchWord: String, position: Int) {
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItemAtPosition<SearchTEViewHolder>(
                    position,
                    click()
                )
            )

        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItemAtPosition<SearchTEViewHolder>(
                    position,
                    typeChildViewWithId(searchWord, R.id.input_editText)
                )
            )
        closeKeyboard()
    }

    fun selectSpecificDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        onView(withId(R.id.datePicker)).perform(
            PickerActions.setDate(
                year,
                monthOfYear,
                dayOfMonth
            )
        )
    }

    fun acceptDate() {
        onView(withId(R.id.acceptBtn)).perform(click())
    }

    fun clickOnSearch() {
        closeKeyboard()
        composeTestRule.onNodeWithTag("SEARCH_BUTTON").performClick()
    }

    fun checkListOfSearchTEI(title: String, attributes: Map<String?, String>) {
        //Checks title and all attributes are displayed
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        attributes.forEach { item ->
            item.key?.let { composeTestRule.onNodeWithText(it).assertIsDisplayed() }
            composeTestRule.onNode(
                hasParent(hasTestTag("LIST_CARD_ADDITIONAL_INFO_COLUMN"))
                        and hasText(item.value), useUnmergedTree = true
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


    fun checkFieldsFromDisplayList(
        displayListFieldsUIModel: DisplayListFieldsUIModel
    ) {
        //Given the title is the first attribute
        val title = "First name: ${displayListFieldsUIModel.name}"
        val displayedAttributes = createAttributesList(displayListFieldsUIModel)

        //When we expand all attribute list
        composeTestRule.onNodeWithText("Show more").performClick()

        //Then The title and all attributes are displayed
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        displayedAttributes.forEach { item ->
            item.key?.let { composeTestRule.onNodeWithText(it).assertIsDisplayed() }
            composeTestRule.onNode(
                hasParent(hasTestTag("LIST_CARD_ADDITIONAL_INFO_COLUMN"))
                        and hasText(item.value), useUnmergedTree = true
            ).assertIsDisplayed()
        }
    }

    fun clickOnShowMap() {
        onView(withId(R.id.navigation_map_view)).perform(click())
    }

    fun checkCarouselTEICardInfo(firstName: String) {
        onView(withId(R.id.map_carousel))
            .check(matches(hasItem(hasDescendant(withText(firstName)))))
    }

    fun clickOnOpenSearch() {
        onView(withId(R.id.openSearchButton)).perform(click())
    }

    fun clickOnEnroll() {
        onView(withId(R.id.createButton)).perform(click())
    }

    private fun createAttributesList(displayListFieldsUIModel: DisplayListFieldsUIModel) = listOf(
        AdditionalInfoItem(
            key = "Last name:",
            value = displayListFieldsUIModel.lastName,
        ),
        AdditionalInfoItem(
            key = "Email:",
            value = displayListFieldsUIModel.email,
        ),
        AdditionalInfoItem(
            key = "Date of birth:",
            value = displayListFieldsUIModel.birthday,
        ),
        AdditionalInfoItem(
            key = "Address:",
            value = displayListFieldsUIModel.address,
        ),
    )
}
