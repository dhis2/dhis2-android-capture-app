package org.dhis2.commons.dialogs.calendarpicker.di

import dagger.Module
import dagger.Provides
import org.dhis2.commons.dialogs.calendarpicker.CalendarPickerRepository
import org.dhis2.commons.dialogs.calendarpicker.CalendarPickerRepositoryImpl
import org.dhis2.commons.prefs.PreferenceProvider

@Module
class CalendarPickerModule {

    @Provides
    fun providesCalendarPickerPresenter(preferences: PreferenceProvider): CalendarPickerRepository {
        return CalendarPickerRepositoryImpl(preferences)
    }
}
