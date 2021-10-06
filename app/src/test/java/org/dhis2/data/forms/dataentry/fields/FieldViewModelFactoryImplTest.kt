package org.dhis2.data.forms.dataentry.fields

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.provider.HintProvider
import org.dhis2.form.ui.provider.LayoutProvider
import org.dhis2.form.ui.style.FormUiColorFactory
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.junit.Before
import org.junit.Test

class FieldViewModelFactoryImplTest {

    private val valueTypeHintMap = HashMap<ValueType, String>()
    private val searchMode = true
    private lateinit var fieldViewModelFactoryImpl: FieldViewModelFactoryImpl
    private val programTrackedEntityAttribute: ProgramTrackedEntityAttribute = mock()
    private val colorFactory: FormUiColorFactory = mock()
    private val layoutProvider: LayoutProvider = mock()
    private val hintProvider: HintProvider = mock()
    private val displayNameProvider: DisplayNameProvider = mock()
    private val trackedEntityAttribute: TrackedEntityAttribute = mock {
        on { uid() } doReturn "1234"
        on { displayFormName() } doReturn "First name"
        on { valueType() } doReturn ValueType.TEXT
    }

    @Before
    fun setUp() {
        valueTypeHintMap[ValueType.TEXT] = "Enter text"
        fieldViewModelFactoryImpl = FieldViewModelFactoryImpl(
            valueTypeHintMap,
            searchMode,
            colorFactory,
            layoutProvider,
            hintProvider,
            displayNameProvider
        )
    }

    @Test
    fun `should display trackedEntityInstanceAttribute as name rather than program attribute`() {
        fieldViewModelFactoryImpl.createForAttribute(
            trackedEntityAttribute,
            programTrackedEntityAttribute,
            "Peter",
            true
        )
        verify(trackedEntityAttribute).displayFormName()
        verify(programTrackedEntityAttribute, never()).displayName()
    }
}
