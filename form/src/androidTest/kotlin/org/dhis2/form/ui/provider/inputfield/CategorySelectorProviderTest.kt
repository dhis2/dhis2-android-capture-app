package org.dhis2.form.ui.provider.inputfield

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.dhis2.form.model.EventCategory
import org.dhis2.form.model.EventCategoryOption
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CategorySelectorProviderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldSelectCategoryOption() {
        val uiModel = generateFieldUiModel(
            uid = "EVENT_CATEGORY_SELECTOR",
            value = "",
            displayName = "Category selector",
            valueType = ValueType.TEXT,
            eventCategories = listOf(
                EventCategory(
                    uid = "category1Uid",
                    name = "category1",
                    options = listOf(
                        EventCategoryOption(
                            uid = "OptionUid1",
                            name = "Option1"
                        )
                    ),
                )
            ),
        )
        uiModel.setCallback(object : FieldUiModel.Callback {

            override fun intent(intent: FormIntent) {
                when (intent) {
                    is FormIntent.OnSave ->
                        assertEquals(intent.value, "OptionUid1")

                    else -> {

                    }
                }
            }

            override fun recyclerViewUiEvents(uiEvent: RecyclerViewUiEvents) {

            }
        })

        composeTestRule.setContent {
            ProvideCategorySelectorInput(
                fieldUiModel = uiModel,
                inputStyle = InputStyle.DataInputStyle(),
            )
        }

        composeTestRule.onNodeWithText("category1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("INPUT_DROPDOWN").performClick()
        composeTestRule.onNodeWithText("Option1").performClick()
    }

    @Test
    fun shouldDisplayTwoCategoryOptions() {
        val uiModel = generateFieldUiModel(
            uid = "EVENT_CATEGORY_SELECTOR",
            value = "optionUid2,optionUid3",
            displayName = "Category selector",
            valueType = ValueType.TEXT,
            eventCategories = listOf(
                EventCategory(
                    uid = "category1Uid",
                    name = "Category1",
                    options = listOf(
                        EventCategoryOption(
                            uid = "optionUid1",
                            name = "Option1"
                        ),
                        EventCategoryOption(
                            uid = "optionUid2",
                            name = "Option2"
                        )
                    ),
                ),
                EventCategory(
                    uid = "category2Uid",
                    name = "Category2",
                    options = listOf(
                        EventCategoryOption(
                            uid = "optionUid3",
                            name = "Option3"
                        ),
                        EventCategoryOption(
                            uid = "optionUid4",
                            name = "Option4"
                        )
                    ),
                )
            ),
        )

        composeTestRule.setContent {
            Column {
                ProvideCategorySelectorInput(
                    fieldUiModel = uiModel,
                    inputStyle = InputStyle.DataInputStyle(),
                )
            }
        }

        composeTestRule.onNodeWithText("Category1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Option2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Category2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Option3").assertIsDisplayed()
    }
}