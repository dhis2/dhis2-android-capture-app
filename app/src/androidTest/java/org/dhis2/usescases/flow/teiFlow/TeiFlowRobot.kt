package org.dhis2.usescases.flow.teiFlow

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.searchte.robot.searchTeiRobot
import org.dhis2.usescases.flow.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.flow.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.teidashboard.robot.enrollmentRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot

fun teiFlowRobot(teiFlowRobot: TeiFlowRobot.() -> Unit) {
    TeiFlowRobot().apply {
        teiFlowRobot()
    }
}

class TeiFlowRobot : BaseRobot() {

    fun registerTEI(
        registrationModel: RegisterTEIUIModel,
        composeTestRule: ComposeContentTestRule
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

    fun enrollToProgram(composeTestRule: ComposeTestRule, program: String) {
        teiDashboardRobot {
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
        teiDashboardRobot {
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

        teiDashboardRobot {
            checkCanNotAddEvent(composeTestRule)
            checkAllEventsAreClosed(composeTestRule)
        }
    }

    fun closeEnrollmentAndCheckEvents(
        composeTestRule: ComposeContentTestRule,
    ) {
        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuComplete()
            checkCanNotAddEvent(composeTestRule)
            checkAllEventsAreClosed(composeTestRule)
        }
    }

    fun changeDueDate(date: String, composeTestRule: ComposeTestRule) {
        teiDashboardRobot {
            clickOnEventGroupByStageUsingDate(composeTestRule, date)
        }

        eventRobot {
            clickOnEventReportDate(composeTestRule)
            selectSpecificDate(composeTestRule, date)
            acceptUpdateEventDate(composeTestRule)
        }
    }
}
