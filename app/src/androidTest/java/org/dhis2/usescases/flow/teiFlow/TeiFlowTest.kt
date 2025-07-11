package org.dhis2.usescases.flow.teiFlow

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.LazyActivityScenarioRule
import org.dhis2.common.mockwebserver.MockWebServerRobot.Companion.API_TRACKED_ENTITY_EMPTY_RESPONSE
import org.dhis2.common.mockwebserver.MockWebServerRobot.Companion.API_TRACKED_ENTITY_PATH
import org.dhis2.commons.date.DateUtils
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.flow.teiFlow.entity.DateRegistrationUIModel
import org.dhis2.usescases.flow.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.flow.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.hisp.dhis.android.core.mockwebserver.ResponseController
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date


@RunWith(AndroidJUnit4::class)
class TeiFlowTest : BaseTest() {
    @get:Rule
    val ruleSearch = lazyActivityScenarioRule<SearchTEActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    private val dateRegistration = createFirstSpecificDate()
    private val dateEnrollment = createEnrollmentDate()
    private val currentDate = getCurrentDate()

    override fun setUp() {
        super.setUp()
        setupMockServer()
    }

    @Test
    fun shouldEnrollToSameProgramAfterClosingIt() {
        enableIntents()
        mockWebServerRobot.addResponse(
            ResponseController.GET,
            API_TRACKED_ENTITY_PATH,
            API_TRACKED_ENTITY_EMPTY_RESPONSE,
        )

        val totalEventsPerEnrollment = 3
        val enrollmentListDetails = createEnrollmentList()
        val registerTeiDetails = createRegisterTEI()

        setupCredentials()
        setDatePicker()
        prepareWomanProgrammeIntentAndLaunchActivity(ruleSearch)

        teiFlowRobot(composeTestRule) {
            registerTEI(registerTeiDetails)
            closeEnrollmentAndCheckEvents()
            composeTestRule.waitForIdle()
            enrollToProgram(ADULT_WOMAN_PROGRAM, enrollmentListDetails)
            checkActiveAndPastEnrollmentDetails(enrollmentListDetails)
            checkPastEventsAreClosed(totalEventsPerEnrollment)
        }
    }

    private fun createEnrollmentList() =
        EnrollmentListUIModel(
            ADULT_WOMAN_PROGRAM,
            ORG_UNIT,
            currentDate,
            currentDate
        )

    private fun createRegisterTEI() = RegisterTEIUIModel(
        NAME,
        LASTNAME,
        dateRegistration,
        dateEnrollment
    )

    private fun createFirstSpecificDate() = DateRegistrationUIModel(
        2016,
        6,
        9
    )

    private fun createEnrollmentDate() = DateRegistrationUIModel(
        2017,
        6,
        9
    )

    private fun getCurrentDate(): String {
        val sdf = DateUtils.uiDateFormat()
        val dateFormat = sdf.format(Date())
        return dateFormat
    }

    private fun prepareWomanProgrammeIntentAndLaunchActivity(
        ruleSearch: LazyActivityScenarioRule<SearchTEActivity>
    ) {
        Intent(
            ApplicationProvider.getApplicationContext(),
            SearchTEActivity::class.java
        ).apply {
            putExtra(PROGRAM_UID, WOMAN_PROGRAM_UID_VALUE)
            putExtra(TE_TYPE, WOMAN_TE_TYPE_VALUE)
        }.also { ruleSearch.launch(it) }
    }

    companion object {
        const val PROGRAM_UID = "PROGRAM_UID"
        const val TE_TYPE = "TRACKED_ENTITY_UID"
        const val WOMAN_PROGRAM_UID_VALUE = "uy2gU8kT1jF"
        const val WOMAN_TE_TYPE_VALUE = "nEenWmSyUEp"

        const val ADULT_WOMAN_PROGRAM = "MNCH / PNC (Adult Woman)"
        const val ORG_UNIT = "Ngelehun CHC"
        const val NAME = "Marta"
        const val LASTNAME = "Stuart"
    }
}