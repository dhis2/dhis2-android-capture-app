package org.dhis2.usescases.settingsprogram

import dagger.Module
import dagger.Provides
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2

@Module
class SettingsProgramModule(val view: ProgramSettingsView) {
    @Provides
    fun providePresenter(d2: D2, schedulersProvider: SchedulerProvider): SettingsProgramPresenter {
        return SettingsProgramPresenter(d2, view, schedulersProvider)
    }

    @Provides
    fun provideAdapter(
        resourceManager: ResourceManager,
        colorUtils: ColorUtils,
    ): SettingsProgramAdapter {
        return SettingsProgramAdapter(resourceManager, colorUtils)
    }
}
