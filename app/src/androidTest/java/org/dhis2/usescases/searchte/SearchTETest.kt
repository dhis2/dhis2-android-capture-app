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
    }
}
