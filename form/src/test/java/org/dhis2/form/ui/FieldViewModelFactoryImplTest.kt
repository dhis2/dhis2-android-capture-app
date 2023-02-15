package org.dhis2.form.ui

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.provider.HintProvider
import org.dhis2.form.ui.provider.KeyboardActionProvider
import org.dhis2.form.ui.provider.LayoutProvider
import org.dhis2.form.ui.provider.LegendValueProvider
import org.dhis2.form.ui.provider.UiEventTypesProvider
import org.dhis2.form.ui.provider.UiStyleProvider
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
    private val uiStyleProvider: UiStyleProvider = mock()
    private val layoutProvider: LayoutProvider = mock()
    private val hintProvider: HintProvider = mock()
    private val displayNameProvider: DisplayNameProvider = mock()
    private val uiEventTypesProvider: UiEventTypesProvider = mock()
    private val keyboardActionProvider: KeyboardActionProvider = mock()
    private val legendValueProvider: LegendValueProvider = mock()
    private val trackedEntityAttribute: TrackedEntityAttribute = mock {
        on { uid() } doReturn "1234"
        on { displayFormName() } doReturn "First name"
        on { valueType() } doReturn ValueType.TEXT
    }

    @Before
    fun setUp() {
        valueTypeHintMap[ValueType.TEXT] = "Enter text"
        fieldViewModelFactoryImpl = FieldViewModelFactoryImpl(
            searchMode,
            uiStyleProvider,
            layoutProvider,
            hintProvider,
            displayNameProvider,
            uiEventTypesProvider,
            keyboardActionProvider,
            legendValueProvider
        )
    }

    @Test
    fun `should display trackedEntityInstanceAttribute as name rather than program attribute`() {
        fieldViewModelFactoryImpl.createForAttribute(
            trackedEntityAttribute,
            programTrackedEntityAttribute,
            "Peter",
            true,
            null
        )
        verify(trackedEntityAttribute).displayFormName()
        verify(programTrackedEntityAttribute, never()).displayName()
    }
}
