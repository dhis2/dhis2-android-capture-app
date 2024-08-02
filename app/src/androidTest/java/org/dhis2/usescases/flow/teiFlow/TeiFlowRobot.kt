package org.dhis2.usescases.flow.teiFlow

import androidx.compose.ui.test.junit4.ComposeTestRule
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.flow.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.flow.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.searchte.robot.searchTeiRobot
import org.dhis2.usescases.teidashboard.robot.enrollmentRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import java.text.SimpleDateFormat
import java.util.Calendar

fun teiFlowRobot(
    composeTestRule: ComposeTestRule,
    teiFlowRobot: TeiFlowRobot.() -> Unit,
) {
    TeiFlowRobot(composeTestRule).apply {
        teiFlowRobot()
    }
}

class TeiFlowRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun registerTEI(
        registrationModel: RegisterTEIUIModel,
    ) {
        val registrationDate = registrationModel.firstSpecificDate
        val incidentDate = getCurrentDate()

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

        enrollmentRobot(composeTestRule) {
            typeOnDateParameterWithLabel("LMP Date *", incidentDate)
            clickOnSaveEnrollment()
        }
    }

    fun enrollToProgram(program: String) {
        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
        }

        enrollmentRobot(composeTestRule) {
            clickOnAProgramForEnrollment(composeTestRule, program)
            clickOnAcceptInDatePicker()
            clickOnSaveEnrollment()
        }
    }

    fun checkActiveAndPastEnrollmentDetails(enrollmentDetails: EnrollmentListUIModel) {
        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
        }

        enrollmentRobot(composeTestRule) {
            waitToDebounce(1000)
            checkActiveAndPastEnrollmentDetails(enrollmentDetails)
        }
    }

    fun checkPastEventsAreClosed(
        programPosition: Int,
    ) {
        enrollmentRobot(composeTestRule) {
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
    ) {
        teiDashboardRobot(composeTestRule) {
            clickOnEventGroupByStageUsingDate(cardTitle)
        }

        eventRobot(composeTestRule) {
            clickOnEventDueDate()
            selectSpecificDate(getCurrentDatePickerDate(), getPreviousDate())
            acceptUpdateEventDate()
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("ddMMYYYY")
        val calendar = Calendar.getInstance()
        return sdf.format(calendar.time)
    }

    private fun getPreviousDate(): String {
        val sdf = SimpleDateFormat("MMddYYYY")
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return sdf.format(calendar.time)
    }

    private fun getCurrentDatePickerDate(): String {
        val sdf = SimpleDateFormat("MM/dd/YYYY")
        val calendar = Calendar.getInstance()
        return sdf.format(calendar.time)
    }
}
