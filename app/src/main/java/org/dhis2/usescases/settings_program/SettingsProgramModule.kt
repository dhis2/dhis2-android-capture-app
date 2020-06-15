package org.dhis2.usescases.settings_program

import dagger.Module
import dagger.Provides
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2

@Module
class SettingsProgramModule(val view: ProgramSettingsView) {
    @Provides
    fun providePresenter(d2: D2, schedulersProvider: SchedulerProvider): SettingsProgramPresenter {
        return SettingsProgramPresenter(d2, view, schedulersProvider)
    }

    @Provides
    fun provideAdapter(resourceManager: ResourceManager): SettingsProgramAdapter {
        return SettingsProgramAdapter(resourceManager)
    }
}