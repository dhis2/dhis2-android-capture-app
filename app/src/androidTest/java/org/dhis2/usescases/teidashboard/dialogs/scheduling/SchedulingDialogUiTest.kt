package org.dhis2.usescases.teidashboard.dialogs.scheduling

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.MutableStateFlow
import org.dhis2.commons.data.EventCreationType
import org.dhis2.composetable.test.TestActivity
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCategory
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.teiDashboard.dialogs.scheduling.SchedulingDialog
import org.dhis2.usescases.teiDashboard.dialogs.scheduling.SchedulingDialogUi
import org.dhis2.usescases.teiDashboard.dialogs.scheduling.SchedulingViewModel
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SchedulingDialogUiTest : BaseTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    private val viewModel: SchedulingViewModel = mock()
    private val enrollment = Enrollment.builder().uid("enrollmentUid").build()
    private val overdueSubtitle = "Overdue subtitle"


    @Before
    override fun setUp() {
        whenever(viewModel.eventDate).thenReturn(MutableStateFlow(EventDate(label = "Date")))
        whenever(viewModel.eventCatCombo).thenReturn(
            MutableStateFlow(
                EventCatCombo(
                    categories = listOf(
                        EventCategory(
                            uid = "uid",
                            name = "CatCombo",
                            optionsSize = 2,
                            options = listOf(
                                CategoryOption.builder().uid("uidA").displayName("optionA").build(),
                                CategoryOption.builder().uid("uidB").displayName("optionB").build(),
                            ),
                        ),
                    ),
                ),
            ),
        )
        whenever(viewModel.overdueEventSubtitle).thenReturn(MutableStateFlow(overdueSubtitle))
    }

    @Test
    fun programStageInputNotDisplayedForOneStage() {
        val programStages =
            listOf(ProgramStage.builder().uid("stageUid").displayName("PS A").build())
        whenever(viewModel.programStage).thenReturn(MutableStateFlow(programStages.first()))
        whenever(viewModel.programStages).thenReturn(MutableStateFlow(programStages))
        whenever(viewModel.enrollment).thenReturn(MutableStateFlow(enrollment))

        composeTestRule.setContent {
            SchedulingDialogUi(
                viewModel = viewModel,
                launchMode = SchedulingDialog.LaunchMode.NewSchedule(
                    enrollmentUid = enrollment.uid(),
                    programStagesUids = programStages.map { it.uid() },
                    ownerOrgUnitUid = null,
                    showYesNoOptions = false,
                    eventCreationType = EventCreationType.SCHEDULE,
                )
            ) {
            }
        }
        composeTestRule.waitForIdle()

        val eventLabel = programStages.first().displayEventLabel() ?: "event"
        composeTestRule.onNodeWithText("Schedule next $eventLabel?")
            .assertExists()
        composeTestRule.onNodeWithText("Program stage").assertDoesNotExist()
        composeTestRule.onNodeWithText("Date").assertExists()
        composeTestRule.onNodeWithText("CatCombo *").assertExists()
        composeTestRule.onNodeWithText("Schedule").assertExists()
    }

    @Test
    fun programStageInputDisplayedForMoreThanOneStages() {
        val programStages = listOf(
            ProgramStage.builder().uid("stageUidA").displayName("PS A").build(),
            ProgramStage.builder().uid("stageUidB").displayName("PS B").build(),
        )
        whenever(viewModel.programStage).thenReturn(MutableStateFlow(programStages.first()))
        whenever(viewModel.programStages).thenReturn(MutableStateFlow(programStages))
        whenever(viewModel.enrollment).thenReturn(MutableStateFlow(enrollment))

        composeTestRule.setContent {
            SchedulingDialogUi(
                viewModel = viewModel,
                launchMode = SchedulingDialog.LaunchMode.NewSchedule(
                    enrollmentUid = enrollment.uid(),
                    programStagesUids = programStages.map { it.uid() },
                    showYesNoOptions = false,
                    eventCreationType = EventCreationType.SCHEDULE,
                    ownerOrgUnitUid = null,
                )
            ) {
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Schedule next event?").assertExists()
        composeTestRule.onNodeWithText("Program stage").assertExists()
    }

    @Test
    fun inputFieldsShouldNotBeDisplayedWhenAnsweringNo() {
        val programStages = listOf(
            ProgramStage.builder().uid("stageUidA").displayName("PS A").build(),
            ProgramStage.builder().uid("stageUidB").displayName("PS B").build(),
        )
        whenever(viewModel.programStage).thenReturn(MutableStateFlow(programStages.first()))
        whenever(viewModel.programStages).thenReturn(MutableStateFlow(programStages))
        whenever(viewModel.enrollment).thenReturn(MutableStateFlow(enrollment))

        composeTestRule.setContent {
            SchedulingDialogUi(
                viewModel = viewModel,
                launchMode = SchedulingDialog.LaunchMode.NewSchedule(
                    enrollmentUid = enrollment.uid(),
                    programStagesUids = programStages.map { it.uid() },
                    showYesNoOptions = true,
                    ownerOrgUnitUid = null,
                    eventCreationType = EventCreationType.SCHEDULE,
                )
            ) {
            }
        }
        composeTestRule.onNodeWithText("No").performClick()
        composeTestRule.onNodeWithText("Program stage").assertDoesNotExist()
        composeTestRule.onNodeWithText("Date").assertDoesNotExist()
        composeTestRule.onNodeWithText("CatCombo *").assertDoesNotExist()
        composeTestRule.onNodeWithText("Done").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun selectProgramStage() {
        val programStages = listOf(
            ProgramStage.builder().uid("stageUidA").displayName("PS A").build(),
            ProgramStage.builder().uid("stageUidB").displayName("PS B").build(),
        )
        whenever(viewModel.programStage).thenReturn(MutableStateFlow(programStages.first()))
        whenever(viewModel.programStages).thenReturn(MutableStateFlow(programStages))
        whenever(viewModel.enrollment).thenReturn(MutableStateFlow(enrollment))

        composeTestRule.setContent {
            SchedulingDialogUi(
                viewModel = viewModel,
                launchMode = SchedulingDialog.LaunchMode.NewSchedule(
                    enrollmentUid = enrollment.uid(),
                    programStagesUids = programStages.map { it.uid() },
                    showYesNoOptions = false,
                    eventCreationType = EventCreationType.SCHEDULE,
                    ownerOrgUnitUid = null,
                )
            ) {
            }
        }

        composeTestRule.onAllNodesWithTag("INPUT_DROPDOWN").onFirst().performClick()
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("INPUT_DROPDOWN_MENU_ITEM_1"))
        composeTestRule.onNodeWithTag(
            testTag = "INPUT_DROPDOWN_MENU_ITEM_1",
            useUnmergedTree = true
        ).performClick()
        composeTestRule.waitForIdle()
        verify(viewModel).updateStage(programStages[1])
    }

    @Test
    fun yesNoFieldsShouldNotBeShownWhenTurnedOff() {
        val programStages = listOf(
            ProgramStage.builder().uid("stageUidA").displayName("PS A").build(),
            ProgramStage.builder().uid("stageUidB").displayName("PS B").build(),
        )
        whenever(viewModel.programStage).thenReturn(MutableStateFlow(programStages.first()))
        whenever(viewModel.programStages).thenReturn(MutableStateFlow(programStages))
        whenever(viewModel.enrollment).thenReturn(MutableStateFlow(enrollment))

        composeTestRule.setContent {
            SchedulingDialogUi(
                viewModel = viewModel,
                launchMode = SchedulingDialog.LaunchMode.NewSchedule(
                    enrollmentUid = enrollment.uid(),
                    programStagesUids = programStages.map { it.uid() },
                    showYesNoOptions = false,
                    eventCreationType = EventCreationType.SCHEDULE,
                    ownerOrgUnitUid = null,
                )
            ) {
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("YES_NO_OPTIONS").assertDoesNotExist()
    }
}
