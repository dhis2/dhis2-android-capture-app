package org.dhis2.usescases.teiFlow

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.typeChildViewWithId
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTEViewHolder
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

    private fun typeOnSearchAtPosition(registerWord: String, position:Int) {
        onView(withId(R.id.form_recycler))
            .perform(
                actionOnItemAtPosition<SearchTEViewHolder>(position, typeChildViewWithId(registerWord, R.id.input_editText))
            )
    }


    private fun clickOnDateField() {
        onView(withId(R.id.form_recycler))
            .perform(
                actionOnItemAtPosition<SearchTEViewHolder>(
                    2,
                    clickChildViewWithId(R.id.inputEditText)
                )
            )
    }

    fun registerTEI(registrationModel: RegisterTEIUIModel) {
        val registrationDate = registrationModel.firstSpecificDate
        val enrollmentDate = registrationModel.enrollmentDate

        typeOnSearchAtPosition(registrationModel.name, 0)
        typeOnSearchAtPosition(registrationModel.lastName,1)
        clickOnDateField()

        searchTeiRobot {
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

    fun checkPastEventsAreClosed(totalEvents: Int) {
        enrollmentRobot {
            clickOnEnrolledProgram(4)
        }

        teiDashboardRobot {
            checkLockCompleteIconIsDisplay()
            checkCanNotAddEvent()
            checkAllEventsAreClosed(totalEvents)
        }
    }

}
