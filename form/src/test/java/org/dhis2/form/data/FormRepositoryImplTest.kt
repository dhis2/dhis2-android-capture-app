package org.dhis2.form.data

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FormRepositoryImplTest {

    private val formValueStore: FormValueStore = mock()
    private val fieldErrorMessageProvider: FieldErrorMessageProvider = mock()
    private val displayNameProvider: DisplayNameProvider = mock()
    private lateinit var repository: FormRepositoryImpl

    @Before
    fun setUp() {
        repository = FormRepositoryImpl(
            formValueStore,
            fieldErrorMessageProvider,
            displayNameProvider
        )
    }

    @Test
    fun `Should process user action ON_FOCUS`() {
        val action = RowAction(
            id = "testUid",
            type = ActionType.ON_FOCUS
        )
        val result = repository.processUserAction(action)
        assertThat(result.valueStoreResult, `is`(ValueStoreResult.VALUE_HAS_NOT_CHANGED))
    }

    @Test
    fun `Should process user action ON_NEXT`() {
        val action = RowAction(
            id = "testUid",
            type = ActionType.ON_NEXT
        )
        val result = repository.processUserAction(action)
        assertThat(result.valueStoreResult, `is`(ValueStoreResult.VALUE_HAS_NOT_CHANGED))
    }

    @Test
    fun `Should process user action ON_TEXT_CHANGE`() {
        val action = RowAction(
            id = "testUid",
            type = ActionType.ON_TEXT_CHANGE
        )
        val result = repository.processUserAction(action)
        assertThat(result.valueStoreResult, `is`(ValueStoreResult.TEXT_CHANGING))
    }

    @Test
    fun `Should process user action ON_SAVE`() {
        val action = RowAction(
            id = "testUid",
            value = "testValue",
            type = ActionType.ON_SAVE
        )
        whenever(formValueStore.save(action.id, action.value, null)) doReturn StoreResult(
            action.id,
            ValueStoreResult.VALUE_CHANGED
        )
        val result = repository.processUserAction(action)
        assertThat(result.valueStoreResult, `is`(ValueStoreResult.VALUE_CHANGED))
    }

    @Test
    fun `Should update not save an item with error when ON_SAVE`() {
        // When user updates a field with error
        val result = repository.processUserAction(
            RowAction(
                id = "testUid",
                value = "testValue",
                type = ActionType.ON_SAVE,
                error = Throwable()
            )
        )

        // Then item should not be saved
        assertThat(result.valueStoreResult, `is`(ValueStoreResult.VALUE_HAS_NOT_CHANGED))
    }

    @Test
    fun `Should set focus to first item`() {
        // Given a list of non focused items
        repository.composeList(provideItemList())

        // When user taps on first item
        repository.processUserAction(
            RowAction(
                id = "uid001",
                value = "value",
                type = ActionType.ON_FOCUS
            )
        )

        // Then result list should has it's first item focused
        assertTrue(repository.composeList()[0].focused)
    }

    @Test
    fun `Should set focus to the next editable item when tapping on next`() {
        // Given a list with first item focused
        repository.composeList(provideItemList())
        repository.processUserAction(
            RowAction(
                id = "uid001",
                value = "value",
                type = ActionType.ON_FOCUS
            )
        )

        // When user taps on next
        repository.processUserAction(
            RowAction(
                id = "uid001",
                value = "value",
                type = ActionType.ON_NEXT
            )
        )

        // Then result list should has second item focused
        assertFalse(repository.composeList()[0].focused)
        assertTrue(repository.composeList()[1].focused)
    }

    @Test
    fun `Should update value when text changes`() {
        // Given a list of items
        repository.composeList(provideItemList())

        // When user updates second item text
        repository.processUserAction(
            RowAction(
                id = "uid002",
                value = "newValue",
                type = ActionType.ON_TEXT_CHANGE
            )
        )

        // Then first item value should has change
        assertThat(repository.composeList()[1].value, `is`("newValue"))
    }

    private fun provideItemList() = listOf<FieldUiModel>(
        FieldUiModelImpl(
            uid = "uid001",
            layoutId = 1,
            value = "value",
            label = "field1",
            valueType = ValueType.TEXT
        ),
        FieldUiModelImpl(
            uid = "uid002",
            layoutId = 2,
            value = "value",
            label = "field2",
            valueType = ValueType.TEXT
        ),
        FieldUiModelImpl(
            uid = "uid003",
            layoutId = 3,
            value = "value",
            label = "field3",
            valueType = ValueType.TEXT
        )
    )
}
