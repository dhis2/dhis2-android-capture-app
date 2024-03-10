package org.dhis2.usescases.flow.teiFlow

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.flow.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.flow.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.searchte.robot.searchTeiRobot
import org.dhis2.usescases.teidashboard.robot.enrollmentRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot

fun teiFlowRobot(
    composeTestRule: ComposeContentTestRule,
    teiFlowRobot: TeiFlowRobot.() -> Unit
) {
    TeiFlowRobot(composeTestRule).apply {
        teiFlowRobot()
    }
}

class TeiFlowRobot(val composeTestRule: ComposeContentTestRule) : BaseRobot() {

    fun registerTEI(
        registrationModel: RegisterTEIUIModel
    ) {
        val registrationDate = registrationModel.firstSpecificDate
        val enrollmentDate = registrationModel.enrollmentDate

        searchTeiRobot(composeTestRule) {
            openNextSearchParameter("First name")
            typeOnNextSearchTextParameter(registrationModel.name)
            openNextSearchParameter("Last name")
            typeOnNextSearchTextParameter(registrationModel.lastName)
            openNextSearchParameter("Date of birth")
            typeOnDateParameter("${registrationDate.day}0${registrationDate.month}${registrationDate.year}")
            clickOnSearch()
            clickOnEnroll()
            selectSpecificDate(enrollmentDate.year, enrollmentDate.month, enrollmentDate.day)
            acceptDate()
        }

        enrollmentRobot {
            clickOnSaveEnrollment()
        }
    }

    fun enrollToProgram(program: String) {
        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
        }

        enrollmentRobot {
            clickOnAProgramForEnrollment(composeTestRule, program)
            clickOnAcceptEnrollmentDate()
            scrollToBottomProgramForm()
            clickOnSaveEnrollment()
        }
    }

    fun checkActiveAndPastEnrollmentDetails(enrollmentDetails: EnrollmentListUIModel) {
        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
        }

        enrollmentRobot {
            waitToDebounce(1000)
            checkActiveAndPastEnrollmentDetails(enrollmentDetails)
        }
    }

    fun checkPastEventsAreClosed(
        composeTestRule: ComposeContentTestRule,
        programPosition: Int
    ) {
        enrollmentRobot {
            clickOnEnrolledProgram(programPosition)
        }

        teiDashboardRobot(composeTestRule) {
            checkCanNotAddEvent()
            checkAllEventsAreClosed()
        }
    }

    fun closeEnrollmentAndCheckEvents(
        composeTestRule: ComposeContentTestRule,
    ) {
        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuComplete()
            checkCanNotAddEvent()
            checkAllEventsAreClosed()
        }
    }

    fun changeDueDate(cardTitle: String, date: String, composeTestRule: ComposeContentTestRule) {
        teiDashboardRobot(composeTestRule) {
            clickOnEventGroupByStageUsingDate(cardTitle)
        }

        eventRobot(composeTestRule) {
            clickOnEventReportDate()
            selectSpecificDate(date)
            acceptUpdateEventDate()
        }
    }
}
