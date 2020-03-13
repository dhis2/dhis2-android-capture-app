package org.dhis2.usescases.settings_program

import dagger.Subcomponent

@Subcomponent(modules = [SettingsProgramModule::class])
interface ProgramSettingsComponent {
    fun inject(activity: SettingsProgramActivity)
}