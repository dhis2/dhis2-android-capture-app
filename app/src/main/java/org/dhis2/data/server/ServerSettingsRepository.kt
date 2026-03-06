package org.dhis2.data.server

import io.reactivex.Single
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.paletteThemes
import org.dhis2.mobile.commons.color.ColorMatcher
import org.dhis2.mobile.commons.color.PaletteColor
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.SystemSetting
import timber.log.Timber

class ServerSettingsRepository(
    private val d2: D2,
    private val colorUtils: ColorUtils,
) {
    fun getTheme(): Single<Pair<String?, Int>> =
        d2
            .settingModule()
            .systemSetting()
            .get()
            .map { systemSettings ->
                val customColor =
                    systemSettings
                        .firstOrNull {
                            it.key() == SystemSetting.SystemSettingKey.CUSTOM_COLOR
                        }?.value()
                val flag =
                    systemSettings
                        .firstOrNull {
                            it.key() == SystemSetting.SystemSettingKey.FLAG
                        }?.value()
                if (customColor.isNullOrEmpty()) {
                    Pair(flag, R.style.AppTheme)
                } else {
                    try {
                        val customColorPalette = PaletteColor.fromHex(customColor)
                        val closestColor =
                            ColorMatcher.findClosest(
                                selectedR = customColorPalette.r,
                                selectedG = customColorPalette.g,
                                selectedB = customColorPalette.b,
                                palette =
                                    paletteThemes.map { (color, _) ->
                                        PaletteColor.fromHex(color)
                                    },
                            )
                        Pair(flag, getThemeFromClosestColor(closestColor))
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing custom color, using default theme")
                        Pair(flag, R.style.AppTheme)
                    }
                }
            }

    private fun getThemeFromClosestColor(color: PaletteColor?): Int =
        color?.let {
            val serverTheme = colorUtils.getThemeFromColor(it.hex)
            if (serverTheme != -1) {
                serverTheme
            } else {
                R.style.AppTheme
            }
        } ?: R.style.AppTheme

    fun allowScreenShare(): Boolean =
        BuildConfig.DEBUG ||
            d2
                .settingModule()
                .generalSetting()
                .blockingGet()
                ?.allowScreenCapture() ?: false
}
