package org.dhis2.usescases.teiFlow

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teiFlow.entity.DateRegistrationUIModel
import org.dhis2.usescases.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.teiFlow.entity.RegisterTEIUIModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date


@RunWith(AndroidJUnit4::class)
class TeiFlowTest: BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)

    @get:Rule
    val ruleSearch = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @Test
    fun shouldEnrollToSameProgramAfterClosedIt() {
        /**
         * MNCH /PNC (Adult Woman)
         * register TEI
         * add event
         * close enrollment, check all event are Program Completed
         * add new enrollment with different date to same program
         * verify enrollment
         * verify there's two enrollments (current and past)
         * check past enrollment all closed events
         * reopen the past enrollment
         * check open
         * check two current enrollments
         * */

        val womanProgram = "MNCH / PNC (Adult Woman)"
        val totalEventsPerEnrollment = 3
        val pastProgramPosition = 4
        val enrollmentListDetails = createEnrollmentList()
        val registerTeiDetails = createRegisterTEI()

        setupCredentials()
        prepareWomanProgrammeIntentAndLaunchActivity(ruleSearch)

        teiFlowRobot {
            registerTEI(registerTeiDetails)
            closeEnrollmentAndCheckEvents(totalEventsPerEnrollment)
            enrollToProgram(womanProgram)
            checkActiveAndPastEnrollmentDetails(enrollmentListDetails)
            checkPastEventsAreClosed(totalEventsPerEnrollment, pastProgramPosition)
        }
    }


    private val dateRegistration = createFirstSpecificDate()
    private val dateEnrollment = createEnrollmentDate()
    private val currentDate = getCurrentDate()

    private fun createEnrollmentList() =
        EnrollmentListUIModel(
            "MNCH / PNC (Adult Woman)",
            "Ngelehun CHC",
            "30/6/2017",
            currentDate
        )

    private fun createRegisterTEI() = RegisterTEIUIModel(
        "Marta",
        "Stuart",
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

    private fun getCurrentDate() :String {
        val sdf = SimpleDateFormat("dd/M/yyyy")
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
    }
}