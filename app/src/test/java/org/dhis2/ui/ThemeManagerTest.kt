package org.dhis2.ui

import io.reactivex.Single
import org.dhis2.commons.Constants
import org.dhis2.commons.R
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.data.server.UserManager
import org.dhis2.metadata.usecases.DataSetConfiguration
import org.dhis2.metadata.usecases.ProgramConfiguration
import org.dhis2.metadata.usecases.TrackedEntityTypeConfiguration
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ThemeManagerTest {
    private val userManager: UserManager = mock()
    private val programConfiguration: ProgramConfiguration = mock()
    private val dataSetConfiguration: DataSetConfiguration = mock()
    private val trackedEntityTypeConfiguration: TrackedEntityTypeConfiguration = mock()
    private val preferenceProvider: PreferenceProvider = mock()
    private val colorUtils: ColorUtils = mock()
    private val themeManager = ThemeManager(
        userManager,
        programConfiguration,
        dataSetConfiguration,
        trackedEntityTypeConfiguration,
        preferenceProvider,
        colorUtils,
    )

    @Test
    fun shouldSetProgramTheme() {
        val programColor = "#ffcdd2"
        val themeColor = R.style.colorPrimary_Pink
        whenever(programConfiguration.getProgramColor("uid")) doReturn programColor
        whenever(colorUtils.getThemeFromColor(programColor)) doReturn themeColor
        themeManager.setProgramTheme("uid")
        verify(programConfiguration).getProgramColor("uid")
        verify(preferenceProvider).setValue(Constants.PROGRAM_THEME, themeColor)
    }

    @Test
    fun shouldRemoveProgramThemeForProgramWithNoColor() {
        val programColor = null
        whenever(programConfiguration.getProgramColor("uid")) doReturn programColor
        whenever(colorUtils.getThemeFromColor(programColor)) doReturn -1
        themeManager.setProgramTheme("uid")
        verify(preferenceProvider).removeValue(Constants.PROGRAM_THEME)
    }

    @Test
    fun shouldSetTrackedEntityTypeTheme() {
        val teTypeColor = "#ffcdd2"
        val themeColor = R.style.colorPrimary_Pink
        whenever(
            trackedEntityTypeConfiguration.getTrackedEntityTypeColor("uid"),
        ) doReturn teTypeColor
        whenever(colorUtils.getThemeFromColor(teTypeColor)) doReturn themeColor
        themeManager.setTrackedEntityTypeTheme("uid")
        verify(trackedEntityTypeConfiguration).getTrackedEntityTypeColor("uid")
        verify(preferenceProvider).setValue(Constants.PROGRAM_THEME, themeColor)
    }

    @Test
    fun shouldRemoveProgramThemeForTrackedEntityTypeWithNoColor() {
        val teTypeColor = "#ffcdd2"
        val themeColor = R.style.colorPrimary_Pink
        whenever(
            trackedEntityTypeConfiguration.getTrackedEntityTypeColor("uid"),
        ) doReturn teTypeColor
        whenever(colorUtils.getThemeFromColor(teTypeColor)) doReturn themeColor
        themeManager.setTrackedEntityTypeTheme("uid")
        verify(trackedEntityTypeConfiguration).getTrackedEntityTypeColor("uid")
        verify(preferenceProvider).setValue(Constants.PROGRAM_THEME, themeColor)
    }

    @Test
    fun shouldReturnProgramThemeColor() {
        val programColor = "#ffcdd2"
        var result = false
        whenever(programConfiguration.getProgramColor("uid")) doReturn programColor
        themeManager.getThemePrimaryColor(
            "uid",
            { result = true },
            { result = false },
        )
        assertTrue(result)
    }

    @Test
    fun shouldReturnAppThemeColorIfProgramIsNull() {
        var result = false
        whenever(userManager.theme)doReturn Single.just(Pair("flag", R.style.AppTheme))
        themeManager.getThemePrimaryColor(
            null,
            { result = false },
            { result = true },
        )
        assertTrue(result)
    }

    @Test
    fun shouldReturnAppThemeColorIfProgramColorIsNull() {
        var result = false
        whenever(programConfiguration.getProgramColor("uid")) doReturn null
        whenever(userManager.theme)doReturn Single.just(Pair("flag", R.style.AppTheme))
        themeManager.getThemePrimaryColor(
            "uid",
            { result = false },
            { result = true },
        )
        assertTrue(result)
    }
}
