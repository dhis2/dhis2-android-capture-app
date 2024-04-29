package org.dhis2.form.ui.event

import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.UiEventType
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Test

class UiEventFactoryImplTest {

    private var uiEventFactory: UiEventFactory? = null

    @Test
    fun `Should return UiEvent OpenCustomCalendar for ValueType DATE`() {
        uiEventFactory = provideEventForType(ValueType.DATE)

        val event = uiEventFactory?.generateEvent(
            value = "2021-09-27",
            uiEventType = UiEventType.DATE_TIME,
            fieldUiModel = provideFieldUiModel(),
        )
        assertThat(
            "Event is OpenCustomCalendar",
            event is RecyclerViewUiEvents.OpenCustomCalendar,
        )
    }

    @Test
    fun `Should return UiEvent OpenCustomCalendar for ValueType DATETIME`() {
        uiEventFactory = provideEventForType(ValueType.DATETIME)

        val event = uiEventFactory?.generateEvent(
            value = "2021-09-27T10:20",
            uiEventType = UiEventType.DATE_TIME,
            fieldUiModel = provideFieldUiModel(),
        )
        assertThat(
            "Event is OpenCustomCalendar",
            event is RecyclerViewUiEvents.OpenCustomCalendar,
        )
    }

    @Test
    fun `Should return UiEvent OpenCustomCalendar for ValueType TIME`() {
        uiEventFactory = provideEventForType(ValueType.TIME)

        val event = uiEventFactory?.generateEvent(
            value = "10:20",
            uiEventType = UiEventType.DATE_TIME,
            fieldUiModel = provideFieldUiModel(),
        )
        assertThat(
            "Event is OpenTimePicker",
            event is RecyclerViewUiEvents.OpenTimePicker,
        )
    }

    private fun provideEventForType(valueType: ValueType) = UiEventFactoryImpl(
        uid = "uid",
        label = "label",
        description = "description",
        valueType = valueType,
        true,
        null,
    )

    private fun provideFieldUiModel() = FieldUiModelImpl(
        uid = "uid",
        label = "label",
        layoutId = 1,
        valueType = ValueType.TEXT,
        optionSetConfiguration = null,
        autocompleteList = null,
    )
}
