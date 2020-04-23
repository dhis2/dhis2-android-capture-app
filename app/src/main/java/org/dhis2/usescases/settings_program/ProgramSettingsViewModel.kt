package org.dhis2.usescases.settings_program

import org.hisp.dhis.android.core.settings.ProgramSetting

data class ProgramSettingsViewModel(
    val programSettings: ProgramSetting,
    val icon: String?,
    val color: String?
)