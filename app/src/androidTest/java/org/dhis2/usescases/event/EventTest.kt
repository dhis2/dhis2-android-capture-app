package org.dhis2.usescases.event

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.UiDevice
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.orgunitselector.orgUnitSelectorRobot
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programevent.robot.programEventsRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventTest : BaseTest() {

    @get:Rule
    val eventListRule = lazyActivityScenarioRule<ProgramEventDetailActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldExerciseEventLifecycle() {
        val event = createFreshFlowAEvent()
        prepareFlowAProgramEventListAndLaunchActivity(eventListRule)

        programEventsRobot(composeTestRule) {
            // [ANDROAPP-917] One card per event in the program.
            waitForEventDisplayed(event.displayDate)
            assertVisibleEventCount(flowAProgramEventCount())

            // [ANDROAPP-904] A data-less event's card shows no report values.
            checkDataLessEventCardHasNoReportValues(
                event.displayDate,
                FLOW_A_ALL_REPORT_DE_MARKERS,
            )
        }

        // [ANDROAPP-4836] List sync button → program granular-sync dialog.
        programEventsRobot(composeTestRule) {
            clickSyncButton()
        }
        composeTestRule.waitForIdle()
        assertFragmentAttached("EVENT_SYNC")
        pressBack()

        // [ANDROAPP-4154] Add (+) opens the new-event flow (org-unit picker).
        programEventsRobot(composeTestRule) {
            clickOnAddEvent()
        }
        composeTestRule.waitForIdle()
        assertFragmentAttached("ORG_UNIT_DIALOG")
        pressBack()

        programEventsRobot(composeTestRule) {
            clickOnEvent(event.displayDate)
        }
        eventRegistrationRobot(composeTestRule) {
            // [ANDROAPP-4647] Save FAB visible when event is editable.
            checkSaveButtonIsDisplayed()
            // [ANDROAPP-1012] Completion-% indicator shown in the corner.
            checkCompletionPercentIsDisplayedInCorner()
            checkOrgUnitIsDisplayed("Ngelehun CHC")
            // [ANDROAPP-4011] DE label uses formName, not displayName.
            checkFieldLabelIsFormName(
                formName = "Date of admission",
                displayName = "Admission Date",
            )
            // [ANDROAPP-5266] Legacy "Update" action button is gone.
            checkNoLegacyUpdateActionIsPresent()
        }

        eventRegistrationRobot(composeTestRule) {
            clickSyncButton()
        }
        composeTestRule.waitForIdle()
        // [ANDROAPP-4837] SyncStatusDialog (tag "EVENT_SYNC") attached.
        assertFragmentAttached("EVENT_SYNC")
        pressBack()
        eventRegistrationRobot(composeTestRule) {
            checkSaveButtonIsDisplayed()
        }

        // ON_COMPLETE positive leg: Save → "Not now" saves ACTIVE, no block.
        eventRobot(composeTestRule) {
            clickOnFormFabButton()
            clickOnNotNow()
        }
        composeTestRule.waitForIdle()
        programEventsRobot(composeTestRule) {
            clickOnEvent(event.displayDate)
        }

        // [ANDROAPP-3832] Complete is blocked while the mandatory DE is empty.
        eventRobot(composeTestRule) {
            clickOnFormFabButton()
            clickOnCompleteButton()
        }
        eventRegistrationRobot(composeTestRule) {
            checkSaveButtonIsDisplayed()
        }

        eventRegistrationRobot(composeTestRule) {
            clickNoOnMandatoryField()
            fillNumberFieldWithLabel(FLOW_A_HEMOGLOBIN_LABEL, FLOW_A_HEMOGLOBIN_VALUE)
            chooseDateForField(FLOW_A_ADMISSION_DATE_LABEL, FLOW_A_ADMISSION_DATE_INPUT)
        }
        composeTestRule.waitForIdle()

        eventRobot(composeTestRule) {
            clickOnFormFabButton()
            clickOnCompleteButton()
        }
        composeTestRule.waitForIdle()

        programEventsRobot(composeTestRule) {
            checkEventIsComplete(event.displayDate)
            // [ANDROAPP-904] The 3 filled DEs show; the empty 4th does not.
            // TODO [ANDROAPP-7723]: expect "No" once the boolean-display bug is fixed.
            checkEventCardReportValues(
                eventDate = event.displayDate,
                expectedEntries = FLOW_A_EXPECTED_CARD_REPORT_ENTRIES,
                expectedCount = FLOW_A_EXPECTED_REPORT_VALUE_COUNT,
            )
        }

        // [ANDROAPP-910] Completed event is read-only (REOPEN_BUTTON shown).
        programEventsRobot(composeTestRule) {
            clickOnEvent(event.displayDate)
        }
        eventRegistrationRobot(composeTestRule) {
            checkFormIsReadOnly()
        }

        // [ANDROAPP-2429] Reopen with authority → back to ACTIVE.
        eventRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            clickOnReopen()
            checkEventIsOpen()
        }
        eventRegistrationRobot(composeTestRule) {
            checkSaveButtonIsDisplayed()
        }

        // [ANDROAPP-1543] Delete via overflow menu → removed from the list.
        eventRobot(composeTestRule) {
            openMenuMoreOptions()
            clickOnDelete()
            clickOnDeleteDialog()
        }
        programEventsRobot(composeTestRule) {
            checkEventWasDeleted(event.displayDate)
        }
    }


    @Test
    fun shouldBlockSaveOnEmptyMandatory() {
        prepareFlowDProgramEventListAndLaunchActivity(eventListRule)

        programEventsRobot(composeTestRule) {
            clickOnAddEvent()
        }
        orgUnitSelectorRobot(composeTestRule) {
            selectTreeOrgUnit(FLOW_D_ORG_UNIT_NAME)
        }
        composeTestRule.waitForIdle()

        eventRegistrationRobot(composeTestRule) {
            // [ANDROAPP-899] Custom event-date label from the stage.
            checkEventDateLabelIsDisplayed(FLOW_D_VISIT_DATE_LABEL)
            // [ANDROAPP-844] Non-default attribute category-combo field.
            checkCategoryFieldIsDisplayed(FLOW_D_CATEGORY_NAME)
        }

        // ON_UPDATE_AND_INSERT: empty mandatory blocks the save outright.
        eventRobot(composeTestRule) {
            clickOnFormFabButton()
        }
        eventRegistrationRobot(composeTestRule) {
            checkImmediateMandatoryBlock()
        }

        // Fill every rendered field, then the same save must succeed.
        eventRegistrationRobot(composeTestRule) {
            dismissMandatoryBlockSheet()
            selectFirstDropdownOption(FLOW_D_CATEGORY_NAME)
            selectFirstDropdownOption(FLOW_D_GENDER_LABEL)
            selectFirstDropdownOption(FLOW_D_RDT_LABEL)
            selectFirstDropdownOption(FLOW_D_TREATMENT_LABEL)
        }

        eventRobot(composeTestRule) {
            clickOnFormFabButton()
        }
        eventRegistrationRobot(composeTestRule) {
            waitForSaveBottomSheet()
        }
        // Complete (not "Not now") — creates the event as COMPLETED.
        eventRobot(composeTestRule) {
            clickOnCompleteButton()
        }
        composeTestRule.waitForIdle()

        // [ANDROAPP-910] New card, dated today, marked "Event completed" in green.
        programEventsRobot(composeTestRule) {
            waitForEventDisplayed(todayDisplayDate())
            checkEventCardStatusColor(todayDisplayDate(), "Event completed", SurfaceColor.CustomGreen)
        }
    }

    private fun assertFragmentAttached(tag: String) {
        val fragment =
            arrayOfNulls<Any>(1).also {
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    val activity =
                        ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(Stage.RESUMED)
                            .firstOrNull() as? FragmentActivity
                    it[0] = activity?.supportFragmentManager?.findFragmentByTag(tag)
                }
            }[0] as? Fragment
        assertNotNull("Expected fragment '$tag' to be attached", fragment)
        assertTrue("Fragment '$tag' must be added", fragment!!.isAdded)
    }

    private fun pressBack() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeTestRule.waitForIdle()
    }
}
