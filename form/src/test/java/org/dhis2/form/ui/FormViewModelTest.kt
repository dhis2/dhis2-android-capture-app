package org.dhis2.form.ui

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.FieldsWithErrorResult
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.GeometryController
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.data.SuccessfulResult
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class FormViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository: FormRepository = mock()
    private val dispatcher: DispatcherProvider = mock {
        on { io() } doReturn Dispatchers.IO
    }
    private val preferenceProvider: PreferenceProvider = mock()
    private val geometryController: GeometryController = mock()

    private lateinit var viewModel: FormViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        viewModel = FormViewModel(
            repository,
            dispatcher,
            geometryController,
            preferenceProvider = preferenceProvider,
        )
    }

    @Ignore("We need to update Kotlin version in order to test coroutines")
    @Test
    fun `should show dialog if a unique field has a coincidence in a unique attribute`() = runTest {
        val storeResult = StoreResult(
            "fieldUid",
            ValueStoreResult.VALUE_NOT_UNIQUE,
        )
//        whenever(repository.processUserAction(any())) doReturn storeResult

        val intent = FormIntent.OnSave(
            uid = "fieldUid",
            value = "123",
            valueType = ValueType.TEXT,
        )
        viewModel.submitIntent(intent)

        assertNotNull("Info message is generated", viewModel.showInfo.value)
    }

    @Ignore("We need to update Kotlin version in order to test coroutines")
    @Test
    fun `Missing and errors fields should show mandatory fields dialog`() {
        whenever(
            repository.runDataIntegrityCheck(false),
        ) doReturn MissingMandatoryResult(
            emptyMap(),
            emptyList(),
            emptyList(),
            false,
            null,
            false,
        )

        viewModel.runDataIntegrityCheck()

        assertTrue(viewModel.dataIntegrityResult.value is MissingMandatoryResult)
    }

    @Ignore("We need to update Kotlin version in order to test coroutines")
    @Test
    fun `Error fields should show mandatory fields dialog`() {
        whenever(
            repository.runDataIntegrityCheck(false),
        ) doReturn FieldsWithErrorResult(
            emptyMap(),
            emptyList(),
            emptyList(),
            false,
            null,
            false,
        )

        viewModel.runDataIntegrityCheck()

        assertTrue(viewModel.dataIntegrityResult.value is FieldsWithErrorResult)
    }

    @Ignore("We need to update Kotlin version in order to test coroutines")
    @Test
    fun `Check data integrity is a success`() {
        whenever(
            repository.runDataIntegrityCheck(false),
        ) doReturn SuccessfulResult(
            null,
            true,
            null,
        )

        viewModel.runDataIntegrityCheck()

        assertTrue(viewModel.dataIntegrityResult.value is SuccessfulResult)
    }

    @Test
    fun `Should return updated value for a field if it has been modified`() {
        val currentData = RowAction(id = "uid", value = "value", type = ActionType.ON_SAVE)
        val uiEvent = RecyclerViewUiEvents.OpenChooserIntent(
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
        val uiEvent = RecyclerViewUiEvents.OpenChooserIntent(
            action = Intent.ACTION_DIAL,
            uid = "uid",
            value = "storedValue",
        )
        viewModel.queryData.value = currentData

        assertTrue(viewModel.getUpdatedData(uiEvent).value == uiEvent.value)
    }
}
