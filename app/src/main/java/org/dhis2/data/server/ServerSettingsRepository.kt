package org.dhis2.data.server

import io.reactivex.Single
import org.dhis2.BuildConfig
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.SystemSetting

class ServerSettingsRepository(
    private val d2: D2,
    private val systemStyleMapper: SystemStyleMapper,
) {

    fun getTheme(): Single<Pair<String?, Int>> {
        return d2.settingModule().systemSetting().get()
            .map { systemSettings ->
                val style =
                    systemSettings.firstOrNull {
                        it.key() == SystemSetting.SystemSettingKey.STYLE
                    }?.value()
                val flag =
                    systemSettings.firstOrNull {
                        it.key() == SystemSetting.SystemSettingKey.FLAG
                    }?.value()
                Pair(flag, systemStyleMapper.map(style))
            }
    }

    fun allowScreenShare(): Boolean {
        return BuildConfig.DEBUG || d2.settingModule().generalSetting().blockingGet()
            ?.allowScreenCapture() ?: false
    }
}
