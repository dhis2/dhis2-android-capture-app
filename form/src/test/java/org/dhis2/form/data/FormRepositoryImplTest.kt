package org.dhis2.form.data

import org.dhis2.form.model.ActionType
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.ValueStoreResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hisp.dhis.android.core.D2
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito

class FormRepositoryImplTest {

    private lateinit var repository: FormRepository
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val recordUid = "ABCD"

    @Before
    fun setUp() {
        repository = FormRepositoryImpl(d2, recordUid)
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
        assertNull(result.valueStoreResult)
    }

    @Ignore("Work in progress, need to mock D2")
    @Test
    fun `Should process user action ON_SAVE`() {
        val action = RowAction(
            id = "testUid",
            value = "testValue",
            type = ActionType.ON_SAVE
        )
        val result = repository.processUserAction(action)
    }
}