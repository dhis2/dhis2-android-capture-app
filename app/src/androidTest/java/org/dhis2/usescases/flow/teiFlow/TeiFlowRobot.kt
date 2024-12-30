package org.dhis2.usescases.flow.teiFlow

import androidx.compose.ui.test.junit4.ComposeTestRule
import org.dhis2.common.BaseRobot
import org.dhis2.common.filters.filterRobotCommon
import org.dhis2.usescases.flow.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.flow.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.orgunitselector.orgUnitSelectorRobot
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
        val orgUnit = "Ngelehun CHC"

        searchTeiRobot(composeTestRule) {
            clickOnOpenSearch()
            openNextSearchParameter("First name")
            typeOnNextSearchTextParameter(registrationModel.name)
            openNextSearchParameter("Last name")
            typeOnNextSearchTextParameter(registrationModel.lastName)
            openNextSearchParameter("Date of birth")
            typeOnDateParameter("0${registrationDate.day}0${registrationDate.month}${registrationDate.year}")
            clickOnSearch()
            clickOnEnroll()
        }

        orgUnitSelectorRobot(composeTestRule) {
            selectTreeOrgUnit(orgUnit)
        }

        enrollmentRobot(composeTestRule) {
            typeOnDateParameterWithLabel("LMP Date *", incidentDate)
            clickOnSaveEnrollment()
        }
    }

    fun enrollToProgram(program: String, enrollmentDetails: EnrollmentListUIModel) {
        val orgUnit = "Ngelehun CHC"

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
        }

        enrollmentRobot(composeTestRule) {
            clickOnAProgramForEnrollment(composeTestRule, program)
        }

        filterRobotCommon {
            val day = enrollmentDetails.currentEnrollmentDate.substring(0, 2)
            val month = enrollmentDetails.currentEnrollmentDate.substring(3, 5)
            val year = enrollmentDetails.currentEnrollmentDate.substring(6)
            selectDate(year.toInt(), month.toInt(), day.toInt())
        }

        orgUnitSelectorRobot(composeTestRule) {
            selectTreeOrgUnit(orgUnit)
        }

        enrollmentRobot(composeTestRule){
            clickOnSaveEnrollment()
        }
    }

    fun checkActiveAndPastEnrollmentDetails(enrollmentDetails: EnrollmentListUIModel) {
        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
        }

        enrollmentRobot(composeTestRule) {
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

        eventRobot(composeTestRule) {
            clickOnEventDueDate()
            selectSpecificDate(getCurrentDatePickerDate(), getPreviousDate())
            acceptUpdateEventDate()
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("ddMMyyyy")
        val calendar = Calendar.getInstance()
        return sdf.format(calendar.time)
    }

    private fun getPreviousDate(): String {
        val sdf = SimpleDateFormat("MMddyyyy")
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return sdf.format(calendar.time)
    }

    private fun getCurrentDatePickerDate(): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val calendar = Calendar.getInstance()
        return sdf.format(calendar.time)
    }
}
