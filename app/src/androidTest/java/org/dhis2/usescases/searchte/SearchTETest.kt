package org.dhis2.usescases.searchte

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import org.dhis2.R
import org.dhis2.common.mockwebserver.MockWebServerRobot.Companion.API_EVENTS_EMPTY_RESPONSE
import org.dhis2.common.mockwebserver.MockWebServerRobot.Companion.API_EVENTS_PATH
import org.dhis2.common.mockwebserver.MockWebServerRobot.Companion.API_TRACKED_ENTITY_EMPTY_RESPONSE
import org.dhis2.common.mockwebserver.MockWebServerRobot.Companion.API_TRACKED_ENTITY_PATH
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.flow.teiFlow.entity.DateRegistrationUIModel
import org.dhis2.usescases.flow.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.flow.teiFlow.teiFlowRobot
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.robot.filterRobot
import org.dhis2.usescases.searchte.robot.searchTeiRobot
import org.hisp.dhis.android.core.mockwebserver.ResponseController
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

class SearchTETest : BaseTest() {

    // Create the rules as fields (not annotated) so we can control their order via RuleChain
    private val rule = lazyActivityScenarioRule<SearchTEActivity>(launchActivity = false)

    private val composeTestRule = createComposeRule()

    // Compose must be inner, so its disposal runs before we close the activity
    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(rule)
        .around(composeTestRule)

    override fun setUp() {
        super.setUp()
        setupMockServer()
    }

    @Test
    fun shouldSuccessfullyFilterByEnrollmentStatusCompleted() {
        val enrollmentStatusFilter = context.getString(R.string.filters_title_enrollment_status)
            .format(
                context.resources.getQuantityString(R.plurals.enrollment, 1)
                    .capitalize(Locale.current),
            )
        val totalFilterCount = "2"
        val filterCount = "1"

        prepareChildProgrammeIntentAndLaunchActivity(rule)

        filterRobot(composeTestRule) {
            openFilters()
            clickOnFilterBy(enrollmentStatusFilter)
            clickOnFilterCompletedOption()
            clickOnSortByField(enrollmentStatusFilter)
            checkFilterCounter(totalFilterCount)
            checkCountAtFilter(enrollmentStatusFilter, filterCount)
            openFilters()
            checkTeiAreCompleted()
        }
    }

    @Test
    fun shouldSuccessfullyFilterByEventStatusOverdue() {
        mockWebServerRobot.addResponse(
            ResponseController.GET,
            API_TRACKED_ENTITY_PATH,
            API_TRACKED_ENTITY_EMPTY_RESPONSE,
        )
        mockWebServerRobot.addResponse(
            ResponseController.GET,
            API_EVENTS_PATH,
            API_EVENTS_EMPTY_RESPONSE,
        )
        val eventStatusFilter = context.getString(R.string.filters_title_event_status)
        val totalCount = "1"
        val registerTeiDetails = createRegisterTEI()

        setDatePicker()
        prepareTestAdultWomanProgrammeIntentAndLaunchActivity(rule)

        teiFlowRobot(composeTestRule) {
            // prepareTestAdultWomanProgrammeIntentAndLaunchActivity() above starts
            // SearchTEActivity; wait for it to be visible before the first Espresso
            // interaction, otherwise the click races the Activity transition.
            waitUntilActivityVisible<SearchTEActivity>()
            registerTEI(registerTeiDetails)
            composeTestRule.waitForIdle()
            pressBack()
        }
        composeTestRule.waitForIdle()
        filterRobot(composeTestRule) {
            openFilters()
            clickOnFilterBy(eventStatusFilter)
            clickOnFilterOverdueOption()
            closeFilterRowAtField(eventStatusFilter)
            checkFilterCounter(totalCount)
            checkCountAtFilter(eventStatusFilter, totalCount)
        }
    }

    @Test
    fun shouldSuccessfullyShowMapAndTeiCard() {
        val firstName = "Rachel"

        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot(composeTestRule) {
            waitUntilActivityVisible<SearchTEActivity>()
            clickOnShowMap()
            checkCarouselTEICardInfo(firstName)
        }
    }

    @Test
    fun shouldFollowTBProgramSearchFlow() {
        mockWebServerRobot.addResponse(
            ResponseController.GET,
            API_TRACKED_ENTITY_PATH,
            API_TRACKED_ENTITY_EMPTY_RESPONSE,
        )

        val searchOutsideAction = context.getString(R.string.search_outside_action)

        prepareTBIntentAndLaunchActivity(rule)

        searchTeiRobot(composeTestRule) {
            waitUntilActivityVisible<SearchTEActivity>()

            // ANDROAPP-5973: create button shown and enabled
            checkAddNewTEIButtonIsDisplayedAndEnabled()

            // ANDROAPP-5971: search button opens the search fields
            clickOnOpenSearch()
            checkSearchParametersAreDisplayed()

            // ANDROAPP-5861, ANDROAPP-6298: unique attribute listed first
            checkFirstSearchParamIsBarcodeOrQROrUnique(UNIQUE_ID_LABEL)

            // ANDROAPP-2458: all 9 searchable attributes listed
            checkSearchParamCount(9)

            // ANDROAPP-5862: search disabled with no values
            checkSearchButtonIsDisabled()

            typeOnSearchParameter(TB_SEARCH_ATTR_CITY, TB_SEARCH_CITY_SHORT)

            // ANDROAPP-5862: search enabled once a value is typed
            checkSearchButtonIsEnabled()

            // ANDROAPP-7488: focused field shows its operator supporting text
            checkFocusedFieldShowsOperatorSupportingText()
            typeOnSearchParameter(TB_SEARCH_ATTR_STATE, TB_SEARCH_CITY_SHORT)
            checkFocusedFieldShowsOperatorSupportingText()
            typeOnSearchParameter(TB_SEARCH_ATTR_TB_NUMBER, TB_SEARCH_CITY_SHORT)

            clickOnSearch()

            // ANDROAPP-7489, ANDROAPP-7491: values below the minimum characters show an error
            checkMinCharactersErrorIsDisplayed(
                TB_SEARCH_ATTR_CITY,
                TB_SEARCH_ATTR_STATE,
                TB_SEARCH_ATTR_TB_NUMBER,
            )

            // ANDROAPP-4834: each field's X erases its value
            clickOnEachFieldResetButton()
            closeKeyboard()
            checkSearchParameterIsEmpty(TB_SEARCH_ATTR_CITY, TB_SEARCH_CITY_SHORT)
            checkSearchParameterIsEmpty(TB_SEARCH_ATTR_STATE, TB_SEARCH_CITY_SHORT)
            checkSearchParameterIsEmpty(TB_SEARCH_ATTR_TB_NUMBER, TB_SEARCH_CITY_SHORT)

            // Valid values
            openNextSearchParameter(TB_SEARCH_ATTR_CITY)
            typeOnSearchParameter(TB_SEARCH_ATTR_CITY, TB_SEARCH_CITY)
            closeKeyboard()
            openNextSearchParameter(TB_SEARCH_ATTR_STATE)
            typeOnSearchParameter(TB_SEARCH_ATTR_STATE, TB_SEARCH_STATE)
            closeKeyboard()
            typeOnSearchParameter(TB_SEARCH_ATTR_TB_NUMBER, TB_SEARCH_TB_NUMBER)

            clickOnSearch()

            // ANDROAPP-7491: values meeting the minimum return Lynn Dunn and Inés Bebea
            checkSearchResultDisplayed(TB_RESULT_DUNN)
            checkSearchResultDisplayed(TB_RESULT_BEBEA)

            // ANDROAPP-5969: search bar lists the searched values
            checkSearchBarShowsQuery(TB_SEARCH_CITY, TB_SEARCH_STATE, TB_SEARCH_TB_NUMBER)

            // ANDROAPP-5863: reopened search keeps the values
            clickOnSearchBar()
            checkSearchParameterHasValue(TB_SEARCH_ATTR_CITY, TB_SEARCH_CITY)
            checkSearchParameterHasValue(TB_SEARCH_ATTR_STATE, TB_SEARCH_STATE)
            checkSearchParameterHasValue(TB_SEARCH_ATTR_TB_NUMBER, TB_SEARCH_TB_NUMBER)

            // ANDROAPP-1033: "Clear search" empties every field
            clickOnClearSearchButton()
            checkSearchParameterIsEmpty(TB_SEARCH_ATTR_CITY, TB_SEARCH_CITY)
            checkSearchParameterIsEmpty(TB_SEARCH_ATTR_STATE, TB_SEARCH_STATE)
            checkSearchParameterIsEmpty(TB_SEARCH_ATTR_TB_NUMBER, TB_SEARCH_TB_NUMBER)

            // ANDROAPP-4055: search by a NUMBER attribute
            typeOnNumberSearchParameter(TB_SEARCH_ATTR_ZIP_CODE, TB_SEARCH_ZIP_CODE)
            clickOnSearch()
            checkSearchResultDisplayed(TB_RESULT_ZIP_CODE)

            // ANDROAPP-5970: resetting the search bar restores the full list
            clickOnResetSearchBar()
            checkSearchBarIsCleared()
            checkSearchResultDisplayed(TB_RESULT_AFTER_RESET)

            // ANDROAPP-6201: no match in TB offers a search outside it, which finds Filona Ryder
            clickOnOpenSearch()
            typeOnSearchParameter(TB_SEARCH_ATTR_FIRST_NAME, OUTSIDE_SEARCH_FIRST_NAME)
            clickOnSearch()
            clickOnSearchOutsideProgram(searchOutsideAction)
            checkSearchResultDisplayed(OUTSIDE_RESULT_LAST_NAME)
        }
    }

    private fun createRegisterTEI() = RegisterTEIUIModel(
        "Claire",
        "Jones",
        dateRegistration,
        dateEnrollment,
    )

    private fun createFirstSpecificDate() = DateRegistrationUIModel(
        2016,
        1,
        9,
    )

    private fun createEnrollmentDate() = DateRegistrationUIModel(
        2020,
        9,
        30,
    )

    private val dateRegistration = createFirstSpecificDate()
    private val dateEnrollment = createEnrollmentDate()

    companion object {
        const val PROGRAM_UID = "PROGRAM_UID"
        const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"

        const val CHILD_TE_TYPE_VALUE = "nEenWmSyUEp"
        const val CHILD_TE_TYPE = "TRACKED_ENTITY_UID"

        // TB Program search flow test constants
        const val TB_IDENTIFIER_LABEL = "TB identifier"
        const val UNIQUE_ID_LABEL = "Unique ID"

        const val TB_SEARCH_ATTR_CITY = "City"
        const val TB_SEARCH_ATTR_STATE = "State"
        const val TB_SEARCH_ATTR_TB_NUMBER = "TB number"

        const val TB_SEARCH_CITY_SHORT = "C"
        const val TB_SEARCH_CITY = "Cit"
        const val TB_SEARCH_STATE = "Sta"
        const val TB_SEARCH_TB_NUMBER = "34567"

        const val TB_RESULT_DUNN = "Dunn"
        const val TB_RESULT_BEBEA = "Bebea"

        const val TB_SEARCH_ATTR_ZIP_CODE = "Zip code"
        const val TB_SEARCH_ZIP_CODE = "4470"
        const val TB_RESULT_ZIP_CODE = "Yohannes"
        const val TB_RESULT_AFTER_RESET = "Palmer"

        // Person enrolled only in Child Programme
        const val TB_SEARCH_ATTR_FIRST_NAME = "First name"
        const val OUTSIDE_SEARCH_FIRST_NAME = "Filona"
        const val OUTSIDE_RESULT_LAST_NAME = "Ryder"
    }
}
