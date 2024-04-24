package org.dhis2.usescases.teiDashboard.data

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.ProgramConfigurationSetting

class ProgramConfigurationRepository(private val d2: D2) {

    fun getConfigurationByProgram(uid: String): ProgramConfigurationSetting? {
        if (d2.settingModule().appearanceSettings().blockingExists()) {
            getSpecificProgramSettings(uid)?.let {
                return it
            } ?: getGlobalConfigurationSettings()?.let {
                return it
            }
        }
        return null
    }

    private fun getGlobalConfigurationSettings() =
        d2.settingModule().appearanceSettings().getGlobalProgramConfigurationSetting()

    private fun getSpecificProgramSettings(uid: String) = d2.settingModule()
        .appearanceSettings()
        .getProgramConfigurationByUid(uid)
}
