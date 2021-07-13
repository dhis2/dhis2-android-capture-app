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
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
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
            waitToDebounce(3000)
        }

        formRobot {
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, HIDE_FIELD, HIDE_FIELD_POSITION)
            checkHiddenField("ZZ TEST LONGTEST")
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, HIDE_SECTION, HIDE_SECTION_POSITION)
            checkHiddenSection("Gamma Rules A")
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, ASSIGN_VALUE, ASSIGN_VALUE_POSITION)
            checkValueWasAssigned(ASSIGNED_VALUE_TEXT)
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, SHOW_WARNING, SHOW_WARNING_POSITION)
            checkWarningIsShown()
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, SHOW_ERROR, SHOW_ERROR_POSITION)
            checkErrorIsShown()
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, WARNING_COMPLETE, WARNING_COMPLETE_POSITION)
            scrollToBottomForm()
            clickOnSaveForm()
            checkPopUpWithMessageOnCompleteIsShown("Warning")
            pressBack()
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, ERROR_COMPLETE, ERROR_COMPLETE_POSITION)
            scrollToBottomForm()
            clickOnSaveForm()
            checkPopUpWithMessageOnCompleteIsShown("Error")
            pressBack()
        }

        formRobot {
            val nonMandatoryLabel = "ZZ TEST NUMBER"
            val mandatoryLabel = "ZZ TEST NUMBER *"
            val position = 4
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            checkLabel(nonMandatoryLabel, position)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, MANDATORY_FIELD, MANDATORY_FIELD_POSITION)
            checkLabel(mandatoryLabel, position)
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, HIDE_OPTION, HIDE_OPTION_POSITION)
            checkHiddenOption("North", OPTION_SET_FIELD_POSITION)
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, HIDE_OPTION_GROUP, HIDE_OPTION_GROUP_POSITION)
            checkHiddenOption("North", OPTION_SET_FIELD_POSITION)
            checkHiddenOption("West", OPTION_SET_FIELD_POSITION)
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, SHOW_OPTION_GROUP, SHOW_OPTION_POSITION)
            checkDisplayedOption("North", OPTION_SET_FIELD_POSITION)
            checkDisplayedOption("West", OPTION_SET_FIELD_POSITION)
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, DISPLAY_TEXT, DISPLAY_TEXT_POSITION)
            pressBack()
            goToAnalytics()
            checkIndicatorIsDisplayed("Info", "Current Option Selected: DT")
            goToDataEntry()
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, DISPLAY_KEY, DISPLAY_KEY_POSITION)
            pressBack()
            goToAnalytics()
            waitToDebounce(1000)
            checkIndicatorIsDisplayed("Current Option", "DKVP")
            goToDataEntry()
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption("ZZ TEST RULE ACTIONS C", 7, HIDE_PROGRAM_STAGE, HIDE_PROGRAM_STAGE_POSITION)
            scrollToPositionForm(0)
            scrollToBottomForm()
            clickOnSaveForm()
            clickOnFinish()
        }
        teiDashboardRobot {
            checkProgramStageIsHidden("Delta")
            clickOnStageGroup("Gamma")
            clickOnEventWithPosition(1)
        }
    }

    @Test
    fun shouldApplyOptionRelatedActions(){
        val rulesFirstSection = "ZZ TEST RULE ACTIONS A"
        val firstSectionPosition = 1
        startSearchActivity(ruleSearch)

        searchTeiRobot {
            clickOnSearchFilter()
            typeAttributeAtPosition("optionGroup", 1)
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
            waitToDebounce(3000)
        }

        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(rulesFirstSection, firstSectionPosition, HIDE_OPTION_GROUP, HIDE_OPTION_GROUP_POSITION)
            checkHiddenOption("North", OPTION_SET_FIELD_POSITION)
            checkHiddenOption("West", OPTION_SET_FIELD_POSITION)
        }
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
        const val OPTION_SET_FIELD_POSITION = 5
        const val HIDE_OPTION_GROUP = "Hide Option Group"
        const val HIDE_OPTION_GROUP_POSITION = 4
        const val ASSIGN_VALUE = "Assign Value"
        const val ASSIGN_VALUE_POSITION = 5
        const val ASSIGNED_VALUE_TEXT = "Result for current event"
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