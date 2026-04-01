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
        // Mock the online search to return empty results (only local DB results shown)
        mockWebServerRobot.addResponse(
            ResponseController.GET,
            API_TRACKED_ENTITY_PATH,
            API_TRACKED_ENTITY_EMPTY_RESPONSE,
        )

        prepareTBIntentAndLaunchActivity(rule)

        searchTeiRobot(composeTestRule) {
            waitUntilActivityVisible<SearchTEActivity>()

            // ANDROAPP-5971: Search/Add new [TET] button is visible and enabled
            checkAddNewTEIButtonIsDisplayedAndEnabled()

            // Open the search parameters panel
            clickOnOpenSearch()

            // ANDROAPP-5861: Unique attribute (TB identifier) is first after sort ordering
            checkFirstSearchParamIsBarcodeOrQROrUnique(TB_IDENTIFIER_LABEL)

            // Check that all 9 search fields are displayed
            checkSearchParamCount(9)

            // ANDROAPP-5862: Search button is disabled when no values are entered
            checkSearchButtonIsDisabled()

            // Enter a value to enable the search button (Part A entry)
            typeOnSearchParameter(TB_SEARCH_ATTR_CITY, TB_SEARCH_CITY_SHORT)

            // [Part A] ANDROAPP-5862: Search button is now enabled
            checkSearchButtonIsEnabled()

            clickOnClearSearch()

            // Re-enter a short value (1 char) to enable the button
            typeOnSearchParameter(TB_SEARCH_ATTR_CITY, TB_SEARCH_CITY_SHORT)
            checkFocusedFieldShowsOperatorSupportingText()
            typeOnSearchParameter(TB_SEARCH_ATTR_STATE, TB_SEARCH_CITY_SHORT)
            checkFocusedFieldShowsOperatorSupportingText()
            typeOnSearchParameter(TB_SEARCH_ATTR_TB_NUMBER, TB_SEARCH_CITY_SHORT)

            // Click Search – triggers per-field min-character validation
            clickOnSearch()

            // ANDROAPP-7489/7490 & ANDROAPP-1056/7491:
            // Error is displayed because value is below the minimum character requirement
            checkMinCharactersErrorIsDisplayed(
                TB_SEARCH_ATTR_CITY,
                TB_SEARCH_ATTR_STATE,
                TB_SEARCH_ATTR_TB_NUMBER,
            )

            // Clear the field
            clickOnClearSearch()

            // Update to valid search values and search again
            typeOnSearchParameter(TB_SEARCH_ATTR_CITY, TB_SEARCH_CITY)
            typeOnSearchParameter(TB_SEARCH_ATTR_STATE, TB_SEARCH_STATE)
            typeOnSearchParameter(TB_SEARCH_ATTR_TB_NUMBER, TB_SEARCH_TB_NUMBER)

            // Click Search with valid values
            clickOnSearch()

            // Verify results: Lynn Dunn and Inés Bebea are displayed
            checkSearchResultDisplayed(TB_RESULT_DUNN)
            checkSearchResultDisplayed(TB_RESULT_BEBEA)
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

        const val TB_SEARCH_ATTR_CITY = "City"
        const val TB_SEARCH_ATTR_STATE = "State"
        const val TB_SEARCH_ATTR_TB_NUMBER = "TB number"

        const val TB_SEARCH_CITY_SHORT = "C"
        const val TB_SEARCH_CITY = "Cit"
        const val TB_SEARCH_STATE = "Sta"
        const val TB_SEARCH_ZIP = "40"
        const val TB_SEARCH_TB_NUMBER = "34567"

        const val TB_RESULT_DUNN = "Dunn"
        const val TB_RESULT_BEBEA = "Bebea"
    }
}
