package org.dhis2.form.ui

import com.nhaarman.mockitokotlin2.mock
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.GeometryController
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class FormViewModelTest {

    private val repository: FormRepository = mock()
    private val dispatcher: DispatcherProvider = mock()
    private val geometryController: GeometryController = mock()

    private lateinit var viewModel: FormViewModel

    @Before
    fun setUp() {
        viewModel = FormViewModel(
            repository,
            dispatcher,
            geometryController
        )
    }

    @Test
    @Ignore("We need to update Kotlin version in order to test coroutines")
    fun `should show dialog if a unique field has a coincidence in a unique attribute`() {
        val action = RowAction(
            id = "fieldUid",
            value = "123",
            type = ActionType.ON_SAVE
        )

        val storeResult = StoreResult(
            "fieldUid",
            ValueStoreResult.VALUE_NOT_UNIQUE
        )
        val result = Pair(action, storeResult)

//        viewModel.displayResult(result)

        assertNotNull("Info message is generated", viewModel.showInfo.value)
    }
}
