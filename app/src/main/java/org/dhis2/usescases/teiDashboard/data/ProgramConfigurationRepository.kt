package org.dhis2.usescases.teiDashboard.data

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.ProgramConfigurationSetting

class ProgramConfigurationRepository(private val d2: D2) {

    fun getConfigurationByProgram(uid: String): ProgramConfigurationSetting? {
        if (d2.settingModule().appearanceSettings().blockingExists()) {
            return d2.settingModule()
                .appearanceSettings()
                .getProgramConfigurationByUid(uid)
        }
        return null
    }
}
