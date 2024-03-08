package org.dhis2.usescases.form

import android.util.Log
import androidx.compose.ui.test.junit4.createComposeRule
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.junit.After
import org.junit.Rule
import org.junit.Test

const val rulesFirstSection = "ZZ TEST RULE ACTIONS A"
const val firstSectionPosition = 1

class FormTest : BaseTest() {

    @get:Rule
    val ruleEvent = lazyActivityScenarioRule<EventCaptureActivity>(launchActivity = false)


    @get:Rule
    val composeTestRule = createComposeRule()

    @After
    override fun teardown() {
        cleanLocalDatabase()
        super.teardown()
    }

    @Test
    fun shouldApplyProgramRules() {
        prepareIntentAndLaunchEventActivity(ruleEvent)

        applyHideField()
        applyHideSection()
        applyShowWarning()
        applyShowError()
        applySetMandatoryField()
        applyHideOption()
        applyHideOptionGroup()
        applyShowOptionGroup()
        applyAssignValue()
        applyDisplayText()
        applyDisplayKeyValue()
        applyWarningOnComplete()
        applyErrorOnComplete()
    }

    private fun applyHideField() {
        formRobot {
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                HIDE_FIELD,
                HIDE_FIELD_POSITION
            )
            checkHiddenField("ZZ TEST LONGTEST")
        }
    }

    private fun applyHideSection() {
        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                HIDE_SECTION,
                HIDE_SECTION_POSITION
            )
            checkHiddenSection("Gamma Rules A")
        }
    }

    private fun applyShowWarning() {
        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                SHOW_WARNING,
                SHOW_WARNING_POSITION
            )
            checkWarningIsShown()
        }
    }

    private fun applyShowError() {
        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                SHOW_ERROR,
                SHOW_ERROR_POSITION
            )
            checkErrorIsShown()
        }
    }

    private fun applySetMandatoryField() {
        formRobot {
            val nonMandatoryLabel = "ZZ TEST NUMBER"
            val mandatoryLabel = "ZZ TEST NUMBER *"
            val position = 4
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            checkLabel(nonMandatoryLabel, position)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                MANDATORY_FIELD,
                MANDATORY_FIELD_POSITION
            )
            checkLabel(mandatoryLabel, position)
        }
    }

    private fun applyHideOption() {
        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                HIDE_OPTION,
                HIDE_OPTION_POSITION
            )
            checkHiddenOption("North", OPTION_SET_FIELD_POSITION)
        }
    }

    private fun applyHideOptionGroup() {
        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                HIDE_OPTION_GROUP,
                HIDE_OPTION_GROUP_POSITION
            )
            checkHiddenOption("North", OPTION_SET_FIELD_POSITION)
            checkHiddenOption("West", OPTION_SET_FIELD_POSITION)
        }
    }

    private fun applyShowOptionGroup() {
        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                SHOW_OPTION_GROUP,
                SHOW_OPTION_POSITION
            )

            val activity = waitForActivityScenario()
            checkDisplayedOption("North", OPTION_SET_FIELD_POSITION, activity)
            checkDisplayedOption("West", OPTION_SET_FIELD_POSITION, activity)
        }
    }

    private fun applyAssignValue() {
        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                ASSIGN_VALUE,
                ASSIGN_VALUE_POSITION
            )
            checkValueWasAssigned(ASSIGNED_VALUE_TEXT)
        }
    }

    private fun applyDisplayText() {
        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                DISPLAY_TEXT,
                DISPLAY_TEXT_POSITION
            )
            pressBack()
            goToAnalytics()
//            waitToDebounce(3000)
            checkIndicatorIsDisplayed("Info", "Current Option Selected: DT")
            goToDataEntry()
        }
    }

    private fun applyDisplayKeyValue() {
        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                DISPLAY_KEY,
                DISPLAY_KEY_POSITION
            )
            pressBack()
            goToAnalytics()
//            waitToDebounce(3000)
            checkIndicatorIsDisplayed("Current Option", "DKVP")
            goToDataEntry()
        }
    }

    private fun applyWarningOnComplete() {
        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                WARNING_COMPLETE,
                WARNING_COMPLETE_POSITION
            )
            scrollToBottomForm()
//            waitToDebounce(1000)
            clickOnSaveForm()
            checkPopUpWithMessageOnCompleteIsShown("WARNING_ON_COMPLETE", composeTestRule)
            pressBack()
        }
    }

    private fun applyErrorOnComplete() {
        formRobot {
            resetToNoAction(rulesFirstSection, firstSectionPosition)
            clickOnSelectOption(
                rulesFirstSection,
                firstSectionPosition,
                ERROR_COMPLETE,
                ERROR_COMPLETE_POSITION
            )
            scrollToBottomForm()
//            waitToDebounce(1000)
            clickOnSaveForm()
            checkPopUpWithMessageOnCompleteIsShown("ERROR_ON_COMPLETE", composeTestRule)
            pressBack()
        }
    }

    private fun waitForActivityScenario(): EventCaptureActivity {
        var activity: EventCaptureActivity? = null
        ruleEvent.getScenario().onActivity {
            activity = it
        }
        while (activity == null) {
            Log.d("FormTest", "Waiting for activity to be initialized")
        }
        return activity!!
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