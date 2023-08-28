package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain

import org.dhis2.ui.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.ui.dialogs.bottomsheet.IssueType
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.provider.EventCaptureResourcesProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ConfigureEventCompletionDialogTest {

    private val provider: EventCaptureResourcesProvider = mock {
        on { provideRedAlertIcon() } doReturn 0
        on { provideYellowAlertIcon() } doReturn 1
        on { provideSavedIcon() } doReturn 2
        on { provideNotSavedText() } doReturn NOT_SAVED
        on { provideSavedText() } doReturn SAVED
        on { provideErrorInfo() } doReturn ERROR_INFO
        on { provideMandatoryInfo() } doReturn "Mandatory Info"
        on { provideMandatoryField() } doReturn "Mandatory field"
        on { provideWarningInfo() } doReturn WARNING_INFO
        on { provideReview() } doReturn 3
        on { provideNotNow() } doReturn 4
        on { provideCompleteInfo() } doReturn COMPLETE_INFO
        on { provideOnCompleteErrorInfo() } doReturn ON_COMPLETE_INFO
    }

    private val fieldWithIssue: FieldWithIssue = mock {
        on { fieldUid } doReturn "uid"
        on { fieldName } doReturn "Field name"
        on { issueType } doReturn IssueType.ERROR
        on { message } doReturn "Message"
    }

    private lateinit var configureEventCompletionDialog: ConfigureEventCompletionDialog

    @Before
    fun setUp() {
        configureEventCompletionDialog = ConfigureEventCompletionDialog(provider)
    }

    @Test
    fun `should show error dialog`() {
        // Given an event form with errors
        val errorFields = listOf(fieldWithIssue)
        val mandatoryFields = mapOf("name" to "")
        val warningFields = listOf(fieldWithIssue)

        // When user tries to complete the event
        val resultDialog = configureEventCompletionDialog.invoke(
            errorFields = errorFields,
            mandatoryFields = mandatoryFields,
            warningFields = warningFields,
            canComplete = true,
            onCompleteMessage = null,
            canSkipErrorFix = true,
        )

        // Then Dialog should has Error info
        assertEquals(resultDialog.bottomSheetDialogUiModel.title, NOT_SAVED)
        assertEquals(resultDialog.bottomSheetDialogUiModel.message, ERROR_INFO)
        assertEquals(resultDialog.bottomSheetDialogUiModel.iconResource, 0)
        assertEquals(resultDialog.fieldsWithIssues.size, 3)
    }

    @Test
    fun `should show mandatory dialog`() {
        // Given an event form with mandatory fields
        val mandatoryFields = mapOf("name" to "")
        val warningFields = listOf(fieldWithIssue)

        // When user tries to complete the event
        val resultDialog = configureEventCompletionDialog.invoke(
            errorFields = emptyList(),
            mandatoryFields = mandatoryFields,
            warningFields = warningFields,
            canComplete = true,
            onCompleteMessage = null,
            canSkipErrorFix = true,
        )

        // Then Dialog should has Error info
        assertEquals(resultDialog.bottomSheetDialogUiModel.title, SAVED)
        assertEquals(resultDialog.bottomSheetDialogUiModel.message, MANDATORY_INFO)
        assertEquals(resultDialog.bottomSheetDialogUiModel.iconResource, 2)
        assertEquals(resultDialog.fieldsWithIssues.size, 2)
    }

    @Test
    fun `should show warning dialog`() {
        // Given an event form with warnings
        val warningFields = listOf(fieldWithIssue)

        // When user tries to complete the event
        val resultDialog = configureEventCompletionDialog.invoke(
            errorFields = emptyList(),
            mandatoryFields = emptyMap(),
            warningFields = warningFields,
            canComplete = true,
            onCompleteMessage = null,
            canSkipErrorFix = true,
        )

        // Then Dialog should has Error info
        assertEquals(resultDialog.bottomSheetDialogUiModel.title, SAVED)
        assertEquals(resultDialog.bottomSheetDialogUiModel.message, WARNING_INFO)
        assertEquals(resultDialog.bottomSheetDialogUiModel.iconResource, 1)
        assertEquals(resultDialog.fieldsWithIssues.size, 1)
    }

    @Test
    fun `should show complete dialog`() {
        // Given an event form without field with issues
        // When user tries to complete the event
        val resultDialog = configureEventCompletionDialog.invoke(
            errorFields = emptyList(),
            mandatoryFields = emptyMap(),
            warningFields = emptyList(),
            canComplete = true,
            onCompleteMessage = null,
            canSkipErrorFix = true,
        )

        // Then Dialog should has Error info
        assertEquals(resultDialog.bottomSheetDialogUiModel.title, SAVED)
        assertEquals(resultDialog.bottomSheetDialogUiModel.message, COMPLETE_INFO)
        assertEquals(resultDialog.bottomSheetDialogUiModel.iconResource, 2)
        assertEquals(resultDialog.fieldsWithIssues.size, 0)
    }

    @Test
    fun `should show complete dialog with warning`() {
        // Given an event form without field with issues
        // When user tries to complete the event
        val resultDialog = configureEventCompletionDialog.invoke(
            errorFields = emptyList(),
            mandatoryFields = emptyMap(),
            warningFields = emptyList(),
            canComplete = true,
            onCompleteMessage = WARNING_MESSAGE,
            canSkipErrorFix = true,
        )

        // Then Dialog should has Error info
        assertEquals(resultDialog.bottomSheetDialogUiModel.title, SAVED)
        assertEquals(resultDialog.bottomSheetDialogUiModel.message, COMPLETE_INFO)
        assertEquals(resultDialog.bottomSheetDialogUiModel.iconResource, 2)
        assertEquals(resultDialog.fieldsWithIssues.size, 1)
    }

    @Test
    fun `should show error on complete`() {
        // Given an event form without field with issues
        // When user tries to complete the event
        val resultDialog = configureEventCompletionDialog.invoke(
            errorFields = emptyList(),
            mandatoryFields = emptyMap(),
            warningFields = emptyList(),
            canComplete = false,
            onCompleteMessage = ERROR_INFO,
            canSkipErrorFix = true,
        )

        // Then Dialog should has Error info
        assertEquals(resultDialog.bottomSheetDialogUiModel.title, SAVED)
        assertEquals(resultDialog.bottomSheetDialogUiModel.message, ON_COMPLETE_INFO)
        assertEquals(resultDialog.bottomSheetDialogUiModel.iconResource, 0)
        assertEquals(resultDialog.fieldsWithIssues.size, 1)
    }

    companion object {
        const val NOT_SAVED = "Not Saved"
        const val SAVED = "Saved"
        const val ERROR_INFO = "Error Info"
        const val MANDATORY_INFO = "Mandatory Info"
        const val WARNING_INFO = "Warning Info"
        const val COMPLETE_INFO = "Complete Info"
        const val WARNING_MESSAGE = "Warning message"
        const val ON_COMPLETE_INFO = "Warning message"
    }
}
