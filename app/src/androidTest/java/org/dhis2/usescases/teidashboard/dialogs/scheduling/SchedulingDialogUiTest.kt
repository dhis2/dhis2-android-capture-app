package org.dhis2.usescases.teidashboard.dialogs.scheduling

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.MutableStateFlow
import org.dhis2.composetable.test.TestActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCategory
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.teiDashboard.dialogs.scheduling.SchedulingDialogUi
import org.dhis2.usescases.teiDashboard.dialogs.scheduling.SchedulingViewModel
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SchedulingDialogUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    private val viewModel: SchedulingViewModel = mock()

    @Before
    fun setUp() {
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
    }

    @Test
    fun programStageInputNotDisplayedForOneStage() {
        val programStages =
            listOf(ProgramStage.builder().uid("stageUid").displayName("PS A").build())
        whenever(viewModel.programStage).thenReturn(MutableStateFlow(programStages.first()))
        composeTestRule.setContent {
            SchedulingDialogUi(
                programStages = programStages,
                viewModel = viewModel,
                orgUnitUid = "orgUnitUid",
            ) {
            }
        }
        composeTestRule.onNodeWithText("Schedule next " + programStages.first().displayName() + "?")
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
        composeTestRule.setContent {
            SchedulingDialogUi(
                programStages = programStages,
                viewModel = viewModel,
                orgUnitUid = "orgUnitUid",
            ) {
            }
        }
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
        composeTestRule.setContent {
            SchedulingDialogUi(
                programStages = programStages,
                viewModel = viewModel,
                orgUnitUid = "orgUnitUid",
            ) {
            }
        }
        composeTestRule.onNodeWithText("No").performClick()

        composeTestRule.onNodeWithText("Program stage").assertDoesNotExist()
        composeTestRule.onNodeWithText("Date").assertDoesNotExist()
        composeTestRule.onNodeWithText("CatCombo *").assertDoesNotExist()
        composeTestRule.onNodeWithText("Done").assertExists()
    }

    @Ignore("Not working")
    @Test
    fun selectProgramStage() {
        val programStages = listOf(
            ProgramStage.builder().uid("stageUidA").displayName("PS A").build(),
            ProgramStage.builder().uid("stageUidB").displayName("PS B").build(),
        )
        whenever(viewModel.programStage).thenReturn(MutableStateFlow(programStages.first()))
        composeTestRule.setContent {
            SchedulingDialogUi(
                programStages = programStages,
                viewModel = viewModel,
                orgUnitUid = "orgUnitUid",
            ) {
            }
        }

        composeTestRule.onNodeWithText("Program stage").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(
            testTag = "INPUT_DROPDOWN_MENU_ITEM_1",
            useUnmergedTree = true
        ).performClick()

        verify(viewModel).updateStage(programStages[1])
    }
}