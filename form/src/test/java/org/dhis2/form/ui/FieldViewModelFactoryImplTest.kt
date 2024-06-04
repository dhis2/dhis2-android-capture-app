package org.dhis2.form.ui

import org.dhis2.form.ui.provider.AutoCompleteProvider
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.provider.HintProvider
import org.dhis2.form.ui.provider.KeyboardActionProvider
import org.dhis2.form.ui.provider.LegendValueProvider
import org.dhis2.form.ui.provider.UiEventTypesProvider
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class FieldViewModelFactoryImplTest {

    private val valueTypeHintMap = HashMap<ValueType, String>()
    private lateinit var fieldViewModelFactoryImpl: FieldViewModelFactoryImpl
    private val programTrackedEntityAttribute: ProgramTrackedEntityAttribute = mock()
    private val hintProvider: HintProvider = mock()
    private val displayNameProvider: DisplayNameProvider = mock()
    private val uiEventTypesProvider: UiEventTypesProvider = mock()
    private val autoCompleteProvider: AutoCompleteProvider = mock()
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
            hintProvider,
            displayNameProvider,
            uiEventTypesProvider,
            keyboardActionProvider,
            legendValueProvider,
            autoCompleteProvider,
        )
    }

    @Test
    fun `should display trackedEntityInstanceAttribute as name rather than program attribute`() {
        fieldViewModelFactoryImpl.create(
            id = trackedEntityAttribute.uid(),
            label = trackedEntityAttribute.displayFormName() ?: "",
            valueType = trackedEntityAttribute.valueType()!!,
            mandatory = false,
            optionSet = trackedEntityAttribute.optionSet()?.uid(),
            value = null,
            programStageSection = null,
            allowFutureDates = programTrackedEntityAttribute.allowFutureDate() ?: true,
            editable = true,
            renderingType = SectionRenderingType.LISTING,
            description = null,
            fieldRendering = programTrackedEntityAttribute.renderType()?.mobile(),
            objectStyle = trackedEntityAttribute.style() ?: ObjectStyle.builder().build(),
            fieldMask = trackedEntityAttribute.fieldMask(),
            optionSetConfiguration = null,
            featureType = null,
            autoCompleteList = null,
            orgUnitSelectorScope = null,
        )
        verify(trackedEntityAttribute).displayFormName()
        verify(programTrackedEntityAttribute, never()).displayName()
    }
}
