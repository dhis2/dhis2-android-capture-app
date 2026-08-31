package org.dhis2.form.ui.provider.inputfield

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Rule
import org.junit.Test

class InputFileProviderTest {

    companion object {
        const val FIELD_UI_MODEL_UID = "FieldUIModelUid"
        const val FILE_RESOURCE_UID = "afl3jai2i4u"
        const val FILE_PATH = "/data/user/0/org.dhis2/files/sdk_resources/db/afl3jai2i4u.pdf"
        const val ORIGINAL_FILE_NAME = "report.pdf"
        const val INPUT_FILE_TEST_TAG = "INPUT_FILE"
        const val FILE_NAME_TEST_TAG = "INPUT_FILE_RESOURCE_UPLOAD_TEXT_FILE_NAME"
        const val ADD_BUTTON_TEST_TAG = "INPUT_FILE_RESOURCE_ADD_BUTTON"
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldDisplayTheOriginalFileNameWhenTheModelHasOne() {
        val fieldUiModel =
            generateFieldUiModel(
                uid = FIELD_UI_MODEL_UID,
                value = FILE_RESOURCE_UID,
                displayName = FILE_PATH,
                valueType = ValueType.FILE_RESOURCE,
                fileName = ORIGINAL_FILE_NAME,
            )

        composeTestRule.setContent {
            ProvideInputFileResource(
                modifier = Modifier.testTag(INPUT_FILE_TEST_TAG),
                fieldUiModel = fieldUiModel,
                onFileSelected = {},
                uiEventHandler = {},
            )
        }

        composeTestRule.onNodeWithTag(FILE_NAME_TEST_TAG).assertTextEquals(ORIGINAL_FILE_NAME)
    }

    @Test
    fun shouldFallBackToThePathFileNameWhenTheModelHasNoFileName() {
        val fieldUiModel =
            generateFieldUiModel(
                uid = FIELD_UI_MODEL_UID,
                value = FILE_RESOURCE_UID,
                displayName = FILE_PATH,
                valueType = ValueType.FILE_RESOURCE,
                fileName = null,
            )

        composeTestRule.setContent {
            ProvideInputFileResource(
                modifier = Modifier.testTag(INPUT_FILE_TEST_TAG),
                fieldUiModel = fieldUiModel,
                onFileSelected = {},
                uiEventHandler = {},
            )
        }

        composeTestRule.onNodeWithTag(FILE_NAME_TEST_TAG).assertTextEquals("$FILE_RESOURCE_UID.pdf")
    }

    @Test
    fun shouldDisplayTheAddButtonWhenThereIsNoValue() {
        val fieldUiModel =
            generateFieldUiModel(
                uid = FIELD_UI_MODEL_UID,
                value = "",
                displayName = null,
                valueType = ValueType.FILE_RESOURCE,
                fileName = null,
            )

        composeTestRule.setContent {
            ProvideInputFileResource(
                modifier = Modifier.testTag(INPUT_FILE_TEST_TAG),
                fieldUiModel = fieldUiModel,
                onFileSelected = {},
                uiEventHandler = {},
            )
        }

        composeTestRule.onNodeWithTag(ADD_BUTTON_TEST_TAG).assertIsDisplayed()
    }
}
