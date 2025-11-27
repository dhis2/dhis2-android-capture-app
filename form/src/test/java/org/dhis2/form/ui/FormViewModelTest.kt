package org.dhis2.form.ui

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.GeometryController
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.provider.FormResultDialogProvider
import org.dhis2.mobile.commons.model.CustomIntentRequestArgumentModel
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ExperimentalCoroutinesApi
class FormViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository: FormRepository = mock()
    private val testingDispatcher = StandardTestDispatcher()
    private val dispatcher: DispatcherProvider =
        mock {
            on { io() } doReturn testingDispatcher
        }
    private val geometryController: GeometryController = mock()
    private val resultDialogUiProvider: FormResultDialogProvider = mock()

    private lateinit var viewModel: FormViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)

        viewModel =
            FormViewModel(
                repository,
                dispatcher,
                geometryController,
                resultDialogUiProvider = resultDialogUiProvider,
            )
        whenever(repository.getDateFormatConfiguration()) doReturn "ddMMyyyy"
    }

    @Test
    fun `Should return updated value for a field if it has been modified`() {
        val currentData = RowAction(id = "uid", value = "value", type = ActionType.ON_SAVE)
        val uiEvent =
            RecyclerViewUiEvents.OpenChooserIntent(
                action = Intent.ACTION_DIAL,
                uid = "uid",
                value = null,
            )
        viewModel.queryData.value = currentData

        assertTrue(viewModel.getUpdatedData(uiEvent).value == currentData.value)
    }

    @Test
    fun `Should return stored value for a field that is not being modified`() {
        val currentData = RowAction(id = "anotherUid", value = "value", type = ActionType.ON_SAVE)
        val uiEvent =
            RecyclerViewUiEvents.OpenChooserIntent(
                action = Intent.ACTION_DIAL,
                uid = "uid",
                value = "storedValue",
            )
        viewModel.queryData.value = currentData

        assertTrue(viewModel.getUpdatedData(uiEvent).value == uiEvent.value)
    }

    @Test
    fun `Should not save last focused item when is not allowed future dates`() =
        runTest {
            val dateField = dateFieldNotAllowedFuture
            whenever(repository.currentFocusedItem()) doReturn dateField
            viewModel.previousActionItem =
                RowAction(
                    id = dateField.uid,
                    value = "2024-12-12",
                    type = ActionType.ON_FOCUS,
                )
            viewModel.submitIntent(FormIntent.OnFocus("newField", null))
            advanceUntilIdle()
            verify(repository).updateErrorList(any())
        }

    @Test
    fun `Should save last focused item with future date when is allowed future dates`() =
        runTest {
            val dateField = dateFieldFuture
            whenever(repository.currentFocusedItem()) doReturn dateField
            viewModel.previousActionItem =
                RowAction(
                    id = dateField.uid,
                    value = "2024-12-12",
                    type = ActionType.ON_FOCUS,
                )
            viewModel.submitIntent(FormIntent.OnFocus("newField", null))
            advanceUntilIdle()
            verify(repository).save(dateField.uid, dateField.value, null)
            verify(repository).updateValueOnList(dateField.uid, dateField.value, dateField.valueType)
        }

    private val futureDate: String = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE)

    private val dateFieldFuture: FieldUiModel =
        mock {
            on { uid } doReturn "fieldUid"
            on { valueType } doReturn ValueType.DATE
            on { allowFutureDates } doReturn true
            on { value } doReturn futureDate
        }

    private val dateFieldNotAllowedFuture: FieldUiModel =
        mock {
            on { uid } doReturn "fieldUid"
            on { valueType } doReturn ValueType.DATE
            on { allowFutureDates } doReturn false
            on { value } doReturn futureDate
        }

    @Test
    fun `Should call repository to get custom intent request params`() {
        val customIntentUid = "custom-intent-uid"
        val expectedParams =
            listOf(
                CustomIntentRequestArgumentModel("param1", "value1"),
                CustomIntentRequestArgumentModel("param2", 123),
            )

        whenever(repository.reEvaluateRequestParams(customIntentUid)) doReturn expectedParams

        val result = viewModel.getCustomIntentRequestParams(customIntentUid)

        assertEquals(expectedParams, result)
        verify(repository).reEvaluateRequestParams(customIntentUid)
    }

    @Test
    fun `Should handle OnSaveCustomIntent with success`() =
        runTest {
            val fieldUid = "field-uid"
            val value = "custom-value"

            viewModel.submitIntent(FormIntent.OnSaveCustomIntent(fieldUid, value, error = false))
            advanceUntilIdle()

            verify(repository).save(fieldUid, value, null)
        }

    @Test
    fun `Should handle OnSaveCustomIntent with error`() =
        runTest {
            val fieldUid = "field-uid"
            val value = "custom-value"

            viewModel.submitIntent(FormIntent.OnSaveCustomIntent(fieldUid, value, error = true))
            advanceUntilIdle()

            verify(repository).updateErrorList(any())
        }

    @Test
    fun `Should handle OnSaveCustomIntent with null value`() =
        runTest {
            val fieldUid = "field-uid"

            viewModel.submitIntent(FormIntent.OnSaveCustomIntent(fieldUid, null, error = false))
            advanceUntilIdle()

            verify(repository).save(fieldUid, null, null)
        }
}
