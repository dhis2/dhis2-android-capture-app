package org.dhis2.commons.customdialogs.di

import dagger.Subcomponent
import org.dhis2.commons.customdialogs.CalendarPicker
import org.dhis2.commons.di.dagger.PerActivity

@PerActivity
@Subcomponent(modules = [CalendarPickerModule::class])
interface CalendarPickerComponent {
    fun inject(calendarPicker: CalendarPicker)
}
