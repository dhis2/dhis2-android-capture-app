package org.dhis2.usescases.flow.teiFlow

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.flow.teiFlow.entity.DateRegistrationUIModel
import org.dhis2.usescases.flow.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.flow.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date


@RunWith(AndroidJUnit4::class)
class TeiFlowTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)

    @get:Rule
    val ruleSearch = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @get:Rule
    val composeTestRule = createComposeRule()

    private val dateRegistration = createFirstSpecificDate()
    private val dateEnrollment = createEnrollmentDate()
    private val currentDate = getCurrentDate()

    @Test
    fun shouldEnrollToSameProgramAfterClosingIt() {
        val totalEventsPerEnrollment = 3
        val enrollmentListDetails = createEnrollmentList()
        val registerTeiDetails = createRegisterTEI()

        setupCredentials()
        setDatePicker()
        prepareWomanProgrammeIntentAndLaunchActivity(ruleSearch)

        teiFlowRobot(composeTestRule) {
            registerTEI(registerTeiDetails)
            closeEnrollmentAndCheckEvents()
            enrollToProgram(ADULT_WOMAN_PROGRAM)
            checkActiveAndPastEnrollmentDetails(enrollmentListDetails)
            checkPastEventsAreClosed(totalEventsPerEnrollment)
        }
    }

    private fun createEnrollmentList() =
        EnrollmentListUIModel(
            ADULT_WOMAN_PROGRAM,
            ORG_UNIT,
            "30/6/2017",
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
        30
    )

    private fun createEnrollmentDate() = DateRegistrationUIModel(
        2017,
        6,
        30
    )

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat(DATE_FORMAT)
        val dateFormat = sdf.format(Date())
        return dateFormat.removePrefix("0")
    }

    private fun prepareWomanProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
        Intent().apply {
            putExtra(PROGRAM_UID, WOMAN_PROGRAM_UID_VALUE)
            putExtra(TE_TYPE, WOMAN_TE_TYPE_VALUE)
        }.also { ruleSearch.launchActivity(it) }
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

        const val DATE_FORMAT = "dd/M/yyyy"
        const val DATE_PICKER_FORMAT = ", d MMMM"

    }
}