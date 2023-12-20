package org.dhis2.form.ui.provider.inputfield

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Test


class DateProviderTest {

    companion object {
        const val DATE_VALUE = "2023-01-19"
        const val FORMATTED_DATE_VALUE = "19012023"

        const val DATE_TIME_VALUE = "2023-01-18T16:23"
        const val FORMATTED_DATE_TIME_VALUE = "180120231623"

        const val TIME_VALUE = "14:11"
        const val FORMATTED_TIME_VALUE = "1411"

        const val INPUT_DATE_TEST_TAG = "INPUT_DATE_TEST_TAG"

        const val FIELD_UI_MODEL_UID = "FieldUIModelUid"

    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldParseADateValueCorrectlyAndDisplayInput() {

        val dateValueTypeFieldUiModel = generateFieldUiModel(FIELD_UI_MODEL_UID,DATE_VALUE,DATE_VALUE, ValueType.DATE)
        composeTestRule.setContent {
            ProvideInputDate(
                modifier = Modifier.testTag(INPUT_DATE_TEST_TAG),
                fieldUiModel = dateValueTypeFieldUiModel,
                intentHandler = {},
                uiEventHandler = {} ,
                onNextClicked = {},
            )

        }
        composeTestRule.onNodeWithTag(INPUT_DATE_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(INPUT_DATE_TEST_TAG).assertContentDescriptionEquals(FORMATTED_DATE_VALUE)

    }

    @Test
    fun shouldParseADateTimeValueCorrectlyAndDisplayInput() {

        val dateValueTypeFieldUiModel = generateFieldUiModel(FIELD_UI_MODEL_UID,DATE_TIME_VALUE,DATE_TIME_VALUE, ValueType.DATETIME)
        composeTestRule.setContent {
            ProvideInputDate(
                modifier = Modifier.testTag(INPUT_DATE_TEST_TAG),
                fieldUiModel = dateValueTypeFieldUiModel,
                intentHandler = {},
                uiEventHandler = {} ,
                onNextClicked = {},
            )
        }
        composeTestRule.onNodeWithTag(INPUT_DATE_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(INPUT_DATE_TEST_TAG).assertContentDescriptionEquals(FORMATTED_DATE_TIME_VALUE)

    }

    @Test
    fun shouldParseATimeValueCorrectlyAndDisplayInput() {

        val dateValueTypeFieldUiModel = generateFieldUiModel(FIELD_UI_MODEL_UID,
            TIME_VALUE,TIME_VALUE, ValueType.TIME)
        composeTestRule.setContent {
            ProvideInputDate(
                modifier = Modifier.testTag(INPUT_DATE_TEST_TAG),
                fieldUiModel = dateValueTypeFieldUiModel,
                intentHandler = {},
                uiEventHandler = {} ,
                onNextClicked = {},
            )
        }
        composeTestRule.onNodeWithTag(INPUT_DATE_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(INPUT_DATE_TEST_TAG).assertContentDescriptionEquals(FORMATTED_TIME_VALUE)

    }
}