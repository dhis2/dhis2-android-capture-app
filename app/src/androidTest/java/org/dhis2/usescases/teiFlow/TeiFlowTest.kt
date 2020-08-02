package org.dhis2.usescases.teiFlow

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teiFlow.entity.DateRegistration
import org.dhis2.usescases.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.teidashboard.prepareWomanProgrammeIntentAndLaunchActivity
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


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

        val enrollmentListDetails = createEnrollmentList()
        val registerTeiDetails = createRegisterTEI()

        setupCredentials()
        prepareWomanProgrammeIntentAndLaunchActivity(ruleSearch)

        teiFlowRobot {
            registerTEI(registerTeiDetails)
        }

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuComplete()
            checkCanNotAddEvent()
            checkAllEventsAreClosed(3)
        }

        teiFlowRobot {
            enrollToProgram(womanProgram)
            checkActiveAndPastEnrollmentDetails(enrollmentListDetails)
            checkPastEventsAreClosed(3)
        }
    }


    val dateRegistration = createFirstSpecificDate()
    val dateEnrollment = createEnrollmentDate()

    private fun createEnrollmentList() =
        EnrollmentListUIModel(
            "MNCH / PNC (Adult Woman)",
            "Ngelehun CHC",
            "30/6/2017",
            ""
        )

    private fun createRegisterTEI() = RegisterTEIUIModel(
        "Marta",
        "Stuart",
        dateRegistration,
        dateEnrollment
    )

    private fun createFirstSpecificDate() = DateRegistration(
        2016,
        6,
        30
    )

    private fun createEnrollmentDate() = DateRegistration(
        2017,
        6,
        30
    )
}