package org.dhis2.ui

import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.data.server.UserManager
import org.dhis2.metadata.usecases.DataSetConfiguration
import org.dhis2.metadata.usecases.ProgramConfiguration
import org.dhis2.metadata.usecases.TrackedEntityTypeConfiguration

class ThemeManager(
    private val userManager: UserManager,
    private val programConfiguration: ProgramConfiguration,
    private val dataSetConfiguration: DataSetConfiguration,
    private val trackedEntityTypeConfiguration: TrackedEntityTypeConfiguration,
    private val preferenceProvider: PreferenceProvider
) {

    fun setProgramTheme(programUid: String) {
        val programColor = getProgramColor(programUid)
        setThemeFromColor(programColor)
    }

    fun getProgramColor(programUid: String) = programConfiguration.getProgramColor(programUid)

    fun setDataSetTheme(dataSetUid: String) {
        val dataSetColor = getDataSetColor(dataSetUid)
        setThemeFromColor(dataSetColor)
    }

    fun getDataSetColor(dataSetUid: String) = dataSetConfiguration.getDataSetColor(dataSetUid)

    fun setTrackedEntityTypeTheme(teTypeUid: String) {
        val teTypeColor = getTeTypeColor(teTypeUid)
        setThemeFromColor(teTypeColor)
    }

    fun getTeTypeColor(teTypeUid: String) =
        trackedEntityTypeConfiguration.getTrackedEntityTypeColor(teTypeUid)

    private fun setThemeFromColor(colorString: String?) {
        val theme = ColorUtils.getThemeFromColor(colorString)
        if (theme != -1) {
            preferenceProvider.setValue(Constants.PROGRAM_THEME, theme)
        } else {
            clearProgramTheme()
        }
    }

    fun clearProgramTheme() {
        preferenceProvider.removeValue(Constants.PROGRAM_THEME)
    }

    fun getAppTheme() = userManager.theme.blockingGet().second

    fun getProgramTheme() = preferenceProvider.getInt(Constants.PROGRAM_THEME, getAppTheme())

    fun getThemePrimaryColor(
        programUid: String?,
        onProgramThemeColor: (programColor: Int) -> Unit,
        onAppThemeColor: (themeColorRes: Int) -> Unit
    ) {
        if (programUid != null) {
            primaryColorForProgramTheme(programUid)?.let {
                onProgramThemeColor(it)
            } ?: onAppThemeColor(primaryColorForAppTheme())
        } else {
            onAppThemeColor(primaryColorForAppTheme())
        }
    }

    private fun primaryColorForProgramTheme(programUid: String): Int? {
        return programConfiguration.getProgramColor(programUid)?.let {
            ColorUtils.parseColor(it)
        }
    }

    private fun primaryColorForAppTheme() = when (getAppTheme()) {
        R.style.AppTheme -> R.color.colorPrimary
        R.style.RedTheme -> R.color.colorPrimaryRed
        R.style.OrangeTheme -> R.color.colorPrimaryOrange
        R.style.GreenTheme -> R.color.colorPrimaryGreen
        else -> R.color.colorPrimary
    }
}
