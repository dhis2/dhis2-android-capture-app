package org.dhis2.usescases.form

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.robot.searchTeiRobot
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.robot.enrollmentRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormTest: BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(EventCaptureActivity::class.java, false, false)

    @get:Rule
    val ruleTeiDashboard = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)

    @get:Rule
    val ruleSearch = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @Test
    fun shouldSuccessfullyUseForm() {
        val rulesFirstSection = "ZZ TEST RULE ACTIONS A"
        val firstSectionPosition = 1
        startSearchActivity(ruleSearch)

        searchTeiRobot {
            clickOnSearchFilter()
            typeAttributeAtPosition("abc", 1)
            clickOnFab()
            clickOnFab()
            selectAnOrgUnit("Ngelehun CHC")
            clickOnAcceptButton()
            acceptDate()
        }

        enrollmentRobot {
            clickOnPersonAttributes("Attributes - Person")
            scrollToBottomProgramForm()
            clickOnDatePicker()
            clickOnAcceptEnrollmentDate()
            clickOnInputDate("DD TEST DATE *")
            clickOnAcceptEnrollmentDate()
            clickOnSaveEnrollment()
        }

        eventRobot {
            clickOnUpdate()
            Thread.sleep(5000)
        }

        formRobot {
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, HIDE_FIELD, HIDE_FIELD_POSITION)
            checkHiddenField(4)
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, HIDE_SECTION, HIDE_SECTION_POSITION)
            checkHiddenSection(3, "Gamma Rules A")
        }

        formRobot {
            resetToNoAction(rulesFirstSection, 1)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, ASSIGN_VALUE, ASSIGN_VALUE_POSITION)
            checkValueWasAssigned()
        }

        formRobot {
            resetToNoAction(rulesFirstSection, 1)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, SHOW_WARNING, SHOW_WARNING_POSITION)
            checkWarningIsShown()
        }

        formRobot {
            resetToNoAction(rulesFirstSection, 1)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, SHOW_ERROR, SHOW_ERROR_POSITION)
            checkErrorIsShown()
        }

        Thread.sleep(10000)
    }


    companion object {
        const val NO_ACTION = "No Action"
        const val NO_ACTION_POSITION = 0
        const val HIDE_FIELD = "Hide Field"
        const val HIDE_FIELD_POSITION = 1
        const val HIDE_SECTION = "Hide Section"
        const val HIDE_SECTION_POSITION = 2
        const val HIDE_OPTION = "Hide Option"
        const val HIDE_OPTION_POSITION = 3
        const val HIDE_OPTION_GROUP = "Hide Option Group"
        const val HIDE_OPTION_GROUP_POSITION = 4
        const val ASSIGN_VALUE = "Assign Value"
        const val ASSIGN_VALUE_POSITION = 5
        const val SHOW_WARNING = "Show Warning"
        const val SHOW_WARNING_POSITION = 6
        const val WARNING_COMPLETE = "Warning on Complete"
        const val WARNING_COMPLETE_POSITION = 7
        const val SHOW_ERROR = "Show Error"
        const val SHOW_ERROR_POSITION = 8
        const val ERROR_COMPLETE = "Error on Complete"
        const val ERROR_COMPLETE_POSITION = 9
        const val MANDATORY_FIELD = "Make Field Mandatory"
        const val MANDATORY_FIELD_POSITION = 10
        const val DISPLAY_TEXT = "Display Text"
        const val DISPLAY_TEXT_POSITION = 11
        const val DISPLAY_KEY = "Display Key/Value Pair"
        const val DISPLAY_KEY_POSITION = 12
        const val HIDE_PROGRAM_STAGE = "Hide Program Stage"
        const val HIDE_PROGRAM_STAGE_POSITION = 13
        const val SHOW_OPTION_GROUP = "Show Option Group"
        const val SHOW_OPTION_POSITION = 14
    }

}