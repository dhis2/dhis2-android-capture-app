package org.dhis2.usescases.main.program

import android.content.Context
import android.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.toColor
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.internal.ImageCardData
import org.junit.Rule
import org.junit.Test

class ProgramUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun shouldShowProgramDescriptionIcon() {
        //Given a program with description
        initProgramItem(programViewModel = provideFakeProgramViewModel())

        //Then description icon is visible
        val programDescription = getString(R.string.program_description)
        composeTestRule.onNodeWithContentDescription(programDescription).assertIsDisplayed()
    }

    @Test
    fun shouldNotShowProgramDescriptionIcon() {
        //Given a program without description
        initProgramItem(
            programViewModel = provideFakeProgramViewModel().copy(description = null)
        )

        //Then description icon is not visible
        val programDescription = getString(R.string.program_description)
        composeTestRule.onNodeWithContentDescription(programDescription).assertDoesNotExist()
    }

    @Test
    fun shouldShowDescriptionDialog() {
        //Given a program with description
        initProgramItem(programViewModel = provideFakeProgramViewModel())

        //When user taps on description icon
        val programDescription = getString(R.string.program_description)
        composeTestRule.onNodeWithContentDescription(programDescription).performClick()

        //Then dialog is shown
        composeTestRule.onNodeWithText(getString(R.string.info)).assertIsDisplayed()
    }

    @Test
    fun shouldDismissDescriptionDialogWhenTapsOnClose() {
        //Given program description dialog is shown
        initProgramItem(programViewModel = provideFakeProgramViewModel())
        val programDescription = getString(R.string.program_description)
        composeTestRule.onNodeWithContentDescription(programDescription).performClick()

        //When user taps on close
        composeTestRule.onNodeWithText(getString(R.string.action_close).uppercase()).performClick()

        //Then dialog is closed
        composeTestRule.onNodeWithText(getString(R.string.info)).assertDoesNotExist()
    }

    private fun getString(stringResource: Int) = context.resources.getString(stringResource)

    private fun initProgramItem(programViewModel: ProgramViewModel) {
        composeTestRule.setContent {
            ProgramItem(programViewModel = programViewModel)
        }
    }

    private fun provideFakeProgramViewModel() =
        ProgramViewModel(
            uid = "qweqwe",
            title = "Program title",
            MetadataIconData(
                imageCardData = ImageCardData.IconCardData("", "", "ic_info", "#00BCD4".toColor()),
                color =  "#00BCD4".toColor(),
            ),
            count = 12,
            type = "type",
            typeName = "Persons",
            programType = "WITH_REGISTRATION",
            description = "Program description",
            onlyEnrollOnce = false,
            accessDataWrite = true,
            state = State.SYNCED,
            hasOverdueEvent = true,
            false,
            downloadState = ProgramDownloadState.NONE,
            stockConfig = null
        )
}