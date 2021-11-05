package org.dhis2.form.ui.event

import com.nhaarman.mockitokotlin2.description
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Test

class UiEventFactoryImplTest {

    private var uiEventFactory: UiEventFactory? = null

    @Test
    fun `Should return UiEvent OpenCustomCalendar for ValueType DATE`() {
        uiEventFactory = provideEventForType(ValueType.DATE)

        val event = uiEventFactory?.generateEvent("27/09/2021")
        assertThat(
            "Event is OpenCustomCalendar",
            event is RecyclerViewUiEvents.OpenCustomCalendar
        )
    }

    @Test
    fun `Should return UiEvent OpenCustomCalendar for ValueType DATETIME`() {
        uiEventFactory = provideEventForType(ValueType.DATETIME)

        val event = uiEventFactory?.generateEvent("2021-09-27 10:20")
        assertThat(
            "Event is OpenCustomCalendar",
            event is RecyclerViewUiEvents.OpenCustomCalendar
        )
    }

    @Test
    fun `Should return UiEvent OpenCustomCalendar for ValueType TIME`() {
        uiEventFactory = provideEventForType(ValueType.TIME)

        val event = uiEventFactory?.generateEvent("10:20")
        assertThat(
            "Event is OpenTimePicker",
            event is RecyclerViewUiEvents.OpenTimePicker
        )
    }

    private fun provideEventForType(valueType: ValueType) = UiEventFactoryImpl(
        uid = "uid",
        label = "label",
        description = "description",
        valueType = valueType,
        true
    )
}
