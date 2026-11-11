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
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.hisp.dhis.android.core.mockwebserver.ResponseController
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import java.text.SimpleDateFormat
import java.util.Calendar

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
            waitUntilActivityVisible<SearchTEActivity>()
            openFilters()
            clickOnSortByField(CHILD_ENROLLMENT_DATE_LABEL)
            openFilters()
        }

        searchTeiRobot(composeTestRule) {
            // ANDROAPP-2431: sorting by enrolment date puts the earliest enrolment first
            checkFirstTEIInList(CHILD_FIRST_BY_ENROLLMENT)

            // ANDROAPP-864: the card shows display-in-list attributes only
            checkTEICardShows(CHILD_FIRST_BY_ENROLLMENT, CHILD_FIRST_BY_ENROLLMENT_LAST_NAME)
            checkTEICardDoesNotShow(CHILD_FIRST_BY_ENROLLMENT, CHILD_ATTR_NOT_IN_LIST)
        }

        filterRobot(composeTestRule) {
            openFilters()
            clickOnSortByField(CHILD_ENROLLMENT_DATE_LABEL)
            openFilters()
        }
        searchTeiRobot(composeTestRule) {
            // ANDROAPP-2431: sorting again reverses the order
            checkFirstTEIInList(CHILD_LAST_BY_ENROLLMENT)
        }

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
        val notSynced = context.getString(R.string.not_synced)
        val totalCount = "1"
        val registerTeiDetails = createRegisterTEI()

        setDatePicker()
        prepareTestAdultWomanProgrammeIntentAndLaunchActivity(rule)

        teiFlowRobot(composeTestRule) {
            waitUntilActivityVisible<SearchTEActivity>()
            // LMP yesterday makes the auto-generated ANC 1st visit overdue by one day
            registerTEI(registerTeiDetails, incidentDate = yesterday())
            composeTestRule.waitForIdle()
            pressBack()
        }
        composeTestRule.waitForIdle()

        searchTeiRobot(composeTestRule) {
            // ANDROAPP-7327: the new TEI shows its overdue status in the list right away
            checkTEICardShows(registerTeiDetails.name, OVERDUE_LABEL, notSynced)
        }

        filterRobot(composeTestRule) {
            openFilters()
            clickOnFilterBy(eventStatusFilter)
            clickOnFilterOverdueOption()
            closeFilterRowAtField(eventStatusFilter)
            checkFilterCounter(totalCount)
            checkCountAtFilter(eventStatusFilter, totalCount)
            openFilters()
        }

        searchTeiRobot(composeTestRule) {
            // ANDROAPP-2431: the overdue filter keeps the overdue TEI in the list
            checkTEICardShows(registerTeiDetails.name, OVERDUE_LABEL, notSynced)
        }

        filterRobot(composeTestRule) {
            openFilters()
            clickOnClearFilters()
            openFilters()
        }

        searchTeiRobot(composeTestRule) {
            // Albert's overdue events are over a year old, outside the overdue filter's window
            clickOnClearSearchButton()
            typeOnSearchParameter(OVERDUE_SEARCH_ATTR_LAST_NAME, OVERDUE_SYNCED_TEI_LAST_NAME)
            clickOnSearch()

            // ANDROAPP-7327: a synced overdue TEI shows the overdue status and isn't flagged for sync
            checkTEICardShows(OVERDUE_SYNCED_TEI, OVERDUE_LABEL)
            checkTEICardDoesNotShow(OVERDUE_SYNCED_TEI, notSynced)

            // ANDROAPP-7327: opening the TEI without changes doesn't flag it for sync
            clickOnTEI(OVERDUE_SYNCED_TEI)
            waitUntilActivityVisible<TeiDashboardMobileActivity>()
            pressBack()
            waitUntilActivityVisible<SearchTEActivity>()
            checkTEICardShows(OVERDUE_SYNCED_TEI, OVERDUE_LABEL)
            checkTEICardDoesNotShow(OVERDUE_SYNCED_TEI, notSynced)
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
        DateRegistrationUIModel(2016, 1, 9),
        DateRegistrationUIModel(2020, 9, 30),
    )

    private fun yesterday(): String =
        SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time)

    companion object {
        const val PROGRAM_UID = "PROGRAM_UID"
        const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"

        const val CHILD_TE_TYPE_VALUE = "nEenWmSyUEp"
        const val CHILD_TE_TYPE = "TRACKED_ENTITY_UID"

        // Child Programme list test constants
        const val CHILD_ENROLLMENT_DATE_LABEL = "Date of enrollment"
        const val CHILD_ATTR_NOT_IN_LIST = "Gender"
        const val CHILD_FIRST_BY_ENROLLMENT = "Ruth"
        const val CHILD_FIRST_BY_ENROLLMENT_LAST_NAME = "Hanson"
        const val CHILD_LAST_BY_ENROLLMENT = "Andrea"

        // Adult Woman overdue test constants
        const val OVERDUE_LABEL = "overdue"
        const val OVERDUE_SYNCED_TEI = "Albert"
        const val OVERDUE_SEARCH_ATTR_LAST_NAME = "Last name"
        const val OVERDUE_SYNCED_TEI_LAST_NAME = "Barnes"

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
