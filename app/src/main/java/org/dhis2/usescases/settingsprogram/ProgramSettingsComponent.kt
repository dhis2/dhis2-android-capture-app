package org.dhis2.usescases.settingsprogram

import dagger.Subcomponent

@Subcomponent(modules = [SettingsProgramModule::class])
interface ProgramSettingsComponent {
    fun inject(activity: SettingsProgramActivity)
}
