package org.dhis2.form.ui.provider.inputfield

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.form.di.Injector
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.junit.Rule
import org.junit.Test


class AgeProviderTest {
     val resourceManager = Injector.provideResourcesManager(InstrumentationRegistry.getInstrumentation().getContext())
    companion object {

        const val AGE_VALUE = "2023-01-19"
        const val INPUT_AGE_RESET_BUTTON = "INPUT_AGE_RESET_BUTTON"
        const val INPUT_AGE = "INPUT_AGE"
        const val INPUT_AGE_MODE_SELECTOR = "INPUT_AGE_MODE_SELECTOR"
        const val DATE_OF_BIRTH = "DATE OF BIRTH"
        const val AGE_BUTTON_TEXT = "AGE"
        const val INPUT_AGE_OPEN_CALENDAR_BUTTON = "INPUT_AGE_OPEN_CALENDAR_BUTTON"
        const val INPUT_AGE_TIME_UNIT_SELECTOR = "INPUT_AGE_TIME_UNIT_SELECTOR"
        const val INPUT_AGE_TEXT_FIELD = "INPUT_AGE_TEXT_FIELD"
        const val RADIO_BUTTON_months = "RADIO_BUTTON_months"
        const val RADIO_BUTTON_days = "RADIO_BUTTON_days"
        const val RADIO_BUTTON_years = "RADIO_BUTTON_years"
        const val AGE_SELECTOR_TEXT = "6 years"
        const val INPUT_AGE_TEST_TAG = "INPUT_AGE"
        const val FIELD_UI_MODEL_UID = "FieldUIModelUid"

    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldDisplayInputAgeCorrectlyWhenModelHasValue() {

        val dateValueTypeFieldUiModel =
            generateFieldUiModel(FIELD_UI_MODEL_UID, AGE_VALUE, AGE_VALUE, ValueType.DATE)
        composeTestRule.setContent {
            ProvideInputAge(
                inputStyle = InputStyle.DataInputStyle(),
                modifier = Modifier.testTag(INPUT_AGE_TEST_TAG),
                fieldUiModel = dateValueTypeFieldUiModel,
                intentHandler = {},
                resources = resourceManager,
            )
        }
        composeTestRule.onNodeWithTag(INPUT_AGE_TEST_TAG).assertIsDisplayed()

    }

    @Test
    fun shouldDisplayTextButtonSelectorWhenValueIsEmptyString() {

        val dateValueTypeFieldUiModel =
            generateFieldUiModel(FIELD_UI_MODEL_UID, "", AGE_VALUE, ValueType.DATE)
        composeTestRule.setContent {
            ProvideInputAge(
                inputStyle = InputStyle.DataInputStyle(),
                modifier = Modifier.testTag(INPUT_AGE_TEST_TAG),
                fieldUiModel = dateValueTypeFieldUiModel,
                intentHandler = {},
                resources = resourceManager,
            )
        }
        composeTestRule.onNodeWithTag(INPUT_AGE)
        composeTestRule.onNodeWithTag(INPUT_AGE_MODE_SELECTOR)

    }

    @Test
    fun shouldDisplayInputDateWhenClickingOnDateButton() {

        val dateValueTypeFieldUiModel =
            generateFieldUiModel(FIELD_UI_MODEL_UID, "", AGE_VALUE, ValueType.DATE)
        composeTestRule.setContent {
            ProvideInputAge(
                inputStyle = InputStyle.DataInputStyle(),

                modifier = Modifier.testTag(INPUT_AGE_TEST_TAG),
                fieldUiModel = dateValueTypeFieldUiModel,
                intentHandler = {},
                resources = resourceManager,
            )
        }
        composeTestRule.onNodeWithTag(INPUT_AGE)
        composeTestRule.onNodeWithTag(INPUT_AGE_MODE_SELECTOR)
        composeTestRule.onNodeWithText(DATE_OF_BIRTH).performClick()
        composeTestRule.onNodeWithTag(INPUT_AGE_OPEN_CALENDAR_BUTTON).assertIsDisplayed()

    }


    @Test
    fun shouldDisplayYearMonthDaySelector() {

        val dateValueTypeFieldUiModel =
            generateFieldUiModel(FIELD_UI_MODEL_UID, "", AGE_VALUE, ValueType.DATE)
        composeTestRule.setContent {
            ProvideInputAge(
                inputStyle = InputStyle.DataInputStyle(),
                modifier = Modifier.testTag(INPUT_AGE_TEST_TAG),
                fieldUiModel = dateValueTypeFieldUiModel,
                intentHandler = {},
                resources = resourceManager,
            )

        }
        composeTestRule.onNodeWithTag(INPUT_AGE)
        composeTestRule.onNodeWithTag(INPUT_AGE_MODE_SELECTOR)
        composeTestRule.onNodeWithText(AGE_BUTTON_TEXT).performClick()
        composeTestRule.onNodeWithTag(INPUT_AGE_TIME_UNIT_SELECTOR).assertIsDisplayed()
    }

    @Test
    fun shouldNotChangeValueWhenUsingAgeSelectorRadioButtons() {

        val dateValueTypeFieldUiModel =
            generateFieldUiModel(FIELD_UI_MODEL_UID, "", AGE_VALUE, ValueType.DATE)
        composeTestRule.setContent {
            ProvideInputAge(
                inputStyle = InputStyle.DataInputStyle(),
                modifier = Modifier.testTag(INPUT_AGE_TEST_TAG),
                fieldUiModel = dateValueTypeFieldUiModel,
                intentHandler = {},
                resources = resourceManager,
            )
        }
        composeTestRule.onNodeWithTag(INPUT_AGE)
        composeTestRule.onNodeWithTag(INPUT_AGE_MODE_SELECTOR)
        composeTestRule.onNodeWithText(AGE_BUTTON_TEXT).performClick()
        composeTestRule.onNodeWithTag(INPUT_AGE_TIME_UNIT_SELECTOR).assertIsDisplayed()
        composeTestRule.onNodeWithTag(INPUT_AGE_TEXT_FIELD).performTextInput("6")
        composeTestRule.onNodeWithTag(RADIO_BUTTON_months).performClick()
        composeTestRule.onNodeWithTag(RADIO_BUTTON_days).performClick()
        composeTestRule.onNodeWithTag(RADIO_BUTTON_years).performClick()
        composeTestRule.onNodeWithTag(INPUT_AGE_TEXT_FIELD).printToLog("AGE_SELECTOR_TEXT")

        composeTestRule.onNodeWithTag(INPUT_AGE_TEXT_FIELD).assertTextEquals(AGE_SELECTOR_TEXT)

    }
    @Test
    fun shouldDisplayTextButtonSelectorWhenTappingResetButton() {

        val dateValueTypeFieldUiModel =
            generateFieldUiModel(FIELD_UI_MODEL_UID, "", AGE_VALUE, ValueType.DATE)
        composeTestRule.setContent {
            ProvideInputAge(
                inputStyle = InputStyle.DataInputStyle(),
                modifier = Modifier.testTag(INPUT_AGE_TEST_TAG),
                fieldUiModel = dateValueTypeFieldUiModel,
                intentHandler = {},
                resources = resourceManager,
            )
        }
        composeTestRule.onNodeWithText(AGE_BUTTON_TEXT).performClick()
        composeTestRule.onNodeWithTag(INPUT_AGE_RESET_BUTTON).assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag(INPUT_AGE_MODE_SELECTOR).assertIsDisplayed()

    }

    @Test
    fun shouldDisplayDatePickerWhenTappingOnCalendarButton() {

        val dateValueTypeFieldUiModel =
            generateFieldUiModel(FIELD_UI_MODEL_UID, AGE_VALUE, AGE_VALUE, ValueType.DATE)
        composeTestRule.setContent {
            ProvideInputAge(
                inputStyle = InputStyle.DataInputStyle(),
                modifier = Modifier.testTag(INPUT_AGE_TEST_TAG),
                fieldUiModel = dateValueTypeFieldUiModel,
                intentHandler = {},
                resources = resourceManager,
            )
        }
        composeTestRule.onNodeWithTag(INPUT_AGE_OPEN_CALENDAR_BUTTON).assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("DATE_PICKER").assertIsDisplayed()

    }

}
