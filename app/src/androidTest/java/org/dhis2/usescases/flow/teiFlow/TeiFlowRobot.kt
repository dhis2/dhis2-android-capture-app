package org.dhis2.usescases.flow.teiFlow

import androidx.compose.ui.test.junit4.ComposeTestRule
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.flow.teiFlow.entity.DateRegistrationUIModel
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

    fun registerTEI(registrationModel: RegisterTEIUIModel) {
        val registrationDate = registrationModel.firstSpecificDate
        val enrollmentDate = registrationModel.enrollmentDate

        searchTeiRobot {
            typeAttributeAtPosition(registrationModel.name, 0)
            typeAttributeAtPosition(registrationModel.lastName, 1)
            clickOnDateField()
            selectSpecificDate(registrationDate.year, registrationDate.month, registrationDate.day)
            acceptDate()
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

    fun checkPastEventsAreClosed(totalEvents: Int, programPosition: Int) {
        enrollmentRobot {
            clickOnEnrolledProgram(programPosition)
        }

        teiDashboardRobot {
            checkLockCompleteIconIsDisplay()
            checkCanNotAddEvent()
            checkAllEventsAreClosed(totalEvents)
        }
    }

    fun closeEnrollmentAndCheckEvents(totalEvents: Int) {
        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuComplete()
            checkCanNotAddEvent()
            checkAllEventsAreClosed(totalEvents)
        }
    }

    fun changeDueDate(date: DateRegistrationUIModel, programStage: String, orgUnit: String) {
        teiDashboardRobot {
            clickOnStageGroup(programStage)
            clickOnEventGroupByStageUsingOU(orgUnit)
        }

        eventRobot {
            clickOnEventDueDate()
            selectSpecificDate(date.year, date.month, date.day)
            acceptUpdateEventDate()
        }
    }
}
