package org.dhis2.form.ui

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.FieldsWithErrorResult
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.GeometryController
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.data.SuccessfulResult
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@ExperimentalCoroutinesApi
class FormViewModelTest {

    /*@get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()*/

    private val repository: FormRepository = mock()
    private val dispatcher: DispatcherProvider = mock {
        on { io() } doReturn Dispatchers.IO
    }
    private val geometryController: GeometryController = mock()

    private lateinit var viewModel: FormViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        viewModel = FormViewModel(
            repository,
            dispatcher,
            geometryController
        )
    }

    @Test
    @Ignore("We need to update Kotlin version in order to test coroutines")
    fun `should show dialog if a unique field has a coincidence in a unique attribute`() = runTest {
        val storeResult = StoreResult(
            "fieldUid",
            ValueStoreResult.VALUE_NOT_UNIQUE
        )
//        whenever(repository.processUserAction(any())) doReturn storeResult

        val intent = FormIntent.OnSave(
            uid = "fieldUid",
            value = "123",
            valueType = ValueType.TEXT
        )
        viewModel.submitIntent(intent)

        assertNotNull("Info message is generated", viewModel.showInfo.value)
    }

    @Test
    @Ignore("We need to update Kotlin version in order to test coroutines")
    fun `Missing and errors fields should show mandatory fields dialog`() {
        whenever(
            repository.runDataIntegrityCheck(false)
        ) doReturn MissingMandatoryResult(
            emptyMap(),
            emptyList(),
            emptyList(),
            false,
            null,
            false
        )

        viewModel.runDataIntegrityCheck()

        assertTrue(viewModel.dataIntegrityResult.value is MissingMandatoryResult)
    }

    @Test
    @Ignore("We need to update Kotlin version in order to test coroutines")
    fun `Error fields should show mandatory fields dialog`() {
        whenever(
            repository.runDataIntegrityCheck(false)
        ) doReturn FieldsWithErrorResult(
            emptyMap(),
            emptyList(),
            emptyList(),
            false,
            null,
            false
        )

        viewModel.runDataIntegrityCheck()

        assertTrue(viewModel.dataIntegrityResult.value is FieldsWithErrorResult)
    }

    @Test
    @Ignore("We need to update Kotlin version in order to test coroutines")
    fun `Check data integrity is a success`() {
        whenever(
            repository.runDataIntegrityCheck(false)
        ) doReturn SuccessfulResult(
            null,
            true,
            null
        )

        viewModel.runDataIntegrityCheck()

        assertTrue(viewModel.dataIntegrityResult.value is SuccessfulResult)
    }
}
