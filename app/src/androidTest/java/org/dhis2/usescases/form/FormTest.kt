package org.dhis2.usescases.form

import android.util.Log
import androidx.compose.ui.test.junit4.createComposeRule
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.junit.After
import org.junit.Rule
import org.junit.Test

const val firstSectionPosition = 2

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

        formRobot {
            clickOnASpecificSection("Gamma Rules A")
        }
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
                firstSectionPosition,
                HIDE_FIELD_POSITION
            )
            checkHiddenField("ZZ TEST LONGTEST")
        }
    }

    private fun applyHideSection() {
        formRobot {
            resetToNoAction(firstSectionPosition)
            clickOnSelectOption(
                firstSectionPosition,
                HIDE_SECTION_POSITION
            )
            checkHiddenSection("Gamma Rules A")
        }
    }

    private fun applyShowWarning() {
        formRobot {
            resetToNoAction(firstSectionPosition)
            clickOnSelectOption(
                firstSectionPosition,
                SHOW_WARNING_POSITION
            )
            checkWarningIsShown()
        }
    }

    private fun applyShowError() {
        formRobot {
            resetToNoAction(firstSectionPosition)
            clickOnSelectOption(
                firstSectionPosition,
                SHOW_ERROR_POSITION
            )
            checkErrorIsShown()
        }
    }

    private fun applySetMandatoryField() {
        formRobot {
            val nonMandatoryLabel = "ZZ TEST NUMBER"
            val mandatoryLabel = "ZZ TEST NUMBER *"
            val position = 5
            resetToNoAction(firstSectionPosition)
            checkLabel(nonMandatoryLabel, position)
            clickOnSelectOption(
                firstSectionPosition,
                MANDATORY_FIELD_POSITION
            )
            checkLabel(mandatoryLabel, position)
        }
    }

    private fun applyHideOption() {
        formRobot {
            resetToNoAction(firstSectionPosition)
            clickOnSelectOption(
                firstSectionPosition,
                HIDE_OPTION_POSITION
            )
            checkHiddenOption("North", OPTION_SET_FIELD_POSITION)
        }
    }

    private fun applyHideOptionGroup() {
        formRobot {
            resetToNoAction(firstSectionPosition)
            clickOnSelectOption(
                firstSectionPosition,
                HIDE_OPTION_GROUP_POSITION
            )
            checkHiddenOption("North", OPTION_SET_FIELD_POSITION)
            checkHiddenOption("West", OPTION_SET_FIELD_POSITION)
        }
    }

    private fun applyShowOptionGroup() {
        formRobot {
            resetToNoAction(firstSectionPosition)
            clickOnSelectOption(
                firstSectionPosition,
                SHOW_OPTION_POSITION
            )

            val activity = waitForActivityScenario()
            checkDisplayedOption("North", OPTION_SET_FIELD_POSITION, activity)
            checkDisplayedOption("West", OPTION_SET_FIELD_POSITION, activity)
        }
    }

    private fun applyAssignValue() {
        formRobot {
            resetToNoAction(firstSectionPosition)
            clickOnSelectOption(
                firstSectionPosition,
                ASSIGN_VALUE_POSITION
            )
            checkValueWasAssigned(ASSIGNED_VALUE_TEXT)
        }
    }

    private fun applyDisplayText() {
        formRobot {
            resetToNoAction(firstSectionPosition)
            clickOnSelectOption(
                firstSectionPosition,
                DISPLAY_TEXT_POSITION
            )
            pressBack()
            goToAnalytics()
            checkIndicatorIsDisplayed("Info", "Current Option Selected: DT")
            goToDataEntry()
        }
    }

    private fun applyDisplayKeyValue() {
        formRobot {
            resetToNoAction(firstSectionPosition)
            clickOnSelectOption(
                firstSectionPosition,
                DISPLAY_KEY_POSITION
            )
            pressBack()
            goToAnalytics()
            checkIndicatorIsDisplayed("Current Option", "DKVP")
            goToDataEntry()
        }
    }

    private fun applyWarningOnComplete() {
        formRobot {
            resetToNoAction(firstSectionPosition)
            clickOnSelectOption(
                firstSectionPosition,
                WARNING_COMPLETE_POSITION
            )
            scrollToBottomForm()
            waitToDebounce(1000)
            clickOnSaveForm()
            checkPopUpWithMessageOnCompleteIsShown("WARNING_ON_COMPLETE", composeTestRule)
            pressBack()
        }
    }

    private fun applyErrorOnComplete() {
        formRobot {
            resetToNoAction(firstSectionPosition)
            clickOnSelectOption(
                firstSectionPosition,
                ERROR_COMPLETE_POSITION
            )
            scrollToBottomForm()
            waitToDebounce(1000)
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
        const val NO_ACTION_POSITION = 0
        const val HIDE_FIELD_POSITION = 1
        const val HIDE_SECTION_POSITION = 2
        const val HIDE_OPTION_POSITION = 3
        const val OPTION_SET_FIELD_POSITION = 6
        const val HIDE_OPTION_GROUP_POSITION = 4
        const val ASSIGN_VALUE_POSITION = 5
        const val ASSIGNED_VALUE_TEXT = "Result for current event"
        const val SHOW_WARNING_POSITION = 6
        const val WARNING_COMPLETE_POSITION = 7
        const val SHOW_ERROR_POSITION = 8
        const val ERROR_COMPLETE_POSITION = 9
        const val MANDATORY_FIELD_POSITION = 10
        const val DISPLAY_TEXT_POSITION = 11
        const val DISPLAY_KEY_POSITION = 12
        const val SHOW_OPTION_POSITION = 14
    }
}