package org.dhis2.usescases.flow.teiFlow

import androidx.compose.ui.test.junit4.ComposeTestRule
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.flow.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.flow.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.searchte.robot.searchTeiRobot
import org.dhis2.usescases.teidashboard.robot.enrollmentRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot

fun teiFlowRobot(
    composeTestRule: ComposeTestRule,
    teiFlowRobot: TeiFlowRobot.() -> Unit
) {
    TeiFlowRobot(composeTestRule).apply {
        teiFlowRobot()
    }
}

class TeiFlowRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

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
        }

        enrollmentRobot {
            clickOnInputDate("Date of enrollment *")
            selectSpecificDate(enrollmentDate.year, enrollmentDate.month, enrollmentDate.day)
            clickOnAcceptInDatePicker()
            clickOnInputDate("LMP Date *")
            clickOnAcceptInDatePicker()
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
            clickOnAcceptInDatePicker()
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

    fun closeEnrollmentAndCheckEvents() {
        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuComplete()
            checkCanNotAddEvent()
            checkAllEventsAreClosed()
        }
    }

    fun changeDueDate(
        cardTitle: String,
        date: String,
    ) {
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
