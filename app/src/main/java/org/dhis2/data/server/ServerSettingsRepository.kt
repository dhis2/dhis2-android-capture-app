package org.dhis2.data.server

import io.reactivex.Single
import org.dhis2.BuildConfig
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.SystemSetting

class ServerSettingsRepository(
    private val d2: D2,
) {
    fun getTheme(): Single<Pair<String?, Int>> =
        d2
            .settingModule()
            .systemSetting()
            .get()
            .map { systemSettings ->
                val style =
                    systemSettings
                        .firstOrNull {
                            it.key() == SystemSetting.SystemSettingKey.STYLE
                        }?.value()
                val flag =
                    systemSettings
                        .firstOrNull {
                            it.key() == SystemSetting.SystemSettingKey.FLAG
                        }?.value()
                Pair(flag, SystemStyleMapper(style))
            }

    fun allowScreenShare(): Boolean =
        BuildConfig.DEBUG ||
            d2
                .settingModule()
                .generalSetting()
                .blockingGet()
                ?.allowScreenCapture() ?: false
}
