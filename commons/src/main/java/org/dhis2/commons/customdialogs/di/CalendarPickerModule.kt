package org.dhis2.commons.customdialogs.di

import dagger.Module
import dagger.Provides
import org.dhis2.commons.customdialogs.CalendarPickerPresenter
import org.dhis2.commons.customdialogs.CalendarPickerPresenterImpl
import org.dhis2.commons.prefs.PreferenceProvider
import javax.inject.Singleton

@Module
class CalendarPickerModule {

    @Provides
    fun providesCalendarPickerPresenter(
        preferences: PreferenceProvider
    ): CalendarPickerPresenter {
        return CalendarPickerPresenterImpl(preferences)
    }
}