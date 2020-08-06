package org.dhis2.usescases.teiFlow


import org.dhis2.common.BaseRobot
import org.dhis2.usescases.searchte.searchTeiRobot
import org.dhis2.usescases.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.teidashboard.robot.enrollmentRobot
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
            searchByPosition(registrationModel.name, 0)
            searchByPosition(registrationModel.lastName, 1)
            clickOnDateField()
            selectSpecificDate(registrationDate.year,registrationDate.month,registrationDate.day)
            acceptDate()
            clickOnFab()
            clickOnFab()
            selectSpecificDate(enrollmentDate.year,enrollmentDate.month,enrollmentDate.day)
            acceptDate()
        }

        enrollmentRobot {
            clickOnSaveEnrollment()
        }
    }

    fun enrollToProgram(program: String) {
        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
        }

        enrollmentRobot {
            clickOnSameProgramForEnrollment(program)
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
}
