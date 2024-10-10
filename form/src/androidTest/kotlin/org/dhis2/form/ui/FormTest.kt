package org.dhis2.form.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.FormSection
import org.dhis2.form.ui.provider.inputfield.AgeProviderTest.Companion.FIELD_UI_MODEL_UID
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.SectionState
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class FormTest {


    companion object {

        const val FORM_VIEW = "FORM_VIEW"
    }


    @get:Rule
    val composeTestRule = createComposeRule()


    @Test
    fun shouldDisplayEnrollmentFormCorrectly() {
        val sections = mutableListOf<FormSection>()
        val fields = listOf(FieldUiModelImpl(
            uid = FIELD_UI_MODEL_UID,
            value = "value",
            focused = false,
            error = null,
            editable = true,
            warning = null,
            mandatory = false,
            label = "label",
            programStageSection = null,
            hint = null,
            description = null,
            valueType = ValueType.AGE,
            legend = null,
            optionSet = null,
            allowFutureDates = null,
            uiEventFactory = null,
            displayName = null,
            renderingType = null,
            keyboardActionType = null,
            fieldMask = null,
            optionSetConfiguration = null,
            autocompleteList = null,
            orgUnitSelectorScope = null,
            selectableDates = null,

        ))
        sections.add(
            FormSection(
                uid = "uid",
                title = "item.label",
                description = "item.description",
                state = SectionState.OPEN,
                fields = fields,
                warningMessage = null,
                completeFields = 0,
                totalFields = 2,
                warnings = 0,
                errors = 0,
            ),
        )

       composeTestRule.setContent {
           Form(
               sections = sections,
               intentHandler = {},
               uiEventHandler = {},
               resources = mock(),
           )
        }
        composeTestRule.onNodeWithTag(FORM_VIEW).assertIsDisplayed()

    }
}