package org.dhis2.ui

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.commons.R
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.metadata.usecases.ProgramConfiguration
import org.dhis2.metadata.usecases.TrackedEntityTypeConfiguration
import org.dhis2.utils.Constants
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeManagerTest {
    private val programConfiguration: ProgramConfiguration = mock()
    private val trackedEntityTypeConfiguration: TrackedEntityTypeConfiguration = mock()
    private val preferenceProvider: PreferenceProvider = mock()
    private val themeManager = ThemeManager(
        programConfiguration,
        trackedEntityTypeConfiguration,
        preferenceProvider
    )

    @Test
    fun shouldSetProgramTheme() {
        val programColor = "#ffcdd2"
        val themeColor = R.style.colorPrimary_Pink
        whenever(programConfiguration.getProgramColor("uid")) doReturn programColor
        themeManager.setProgramTheme("uid")
        verify(programConfiguration).getProgramColor("uid")
        verify(preferenceProvider).setValue(Constants.PROGRAM_THEME, themeColor)
    }

    @Test
    fun shouldRemoveProgramThemeForProgramWithNoColor() {
        val programColor = null
        whenever(programConfiguration.getProgramColor("uid")) doReturn programColor
        themeManager.setProgramTheme("uid")
        verify(preferenceProvider).removeValue(Constants.PROGRAM_THEME)
    }

    @Test
    fun shouldSetTrackedEntityTypeTheme() {
        val teTypeColor = "#ffcdd2"
        val themeColor = R.style.colorPrimary_Pink
        whenever(
            trackedEntityTypeConfiguration.getTrackedEntityTypeColor("uid")
        ) doReturn teTypeColor
        themeManager.setTrackedEntityTypeTheme("uid")
        verify(trackedEntityTypeConfiguration).getTrackedEntityTypeColor("uid")
        verify(preferenceProvider).setValue(Constants.PROGRAM_THEME, themeColor)
    }

    @Test
    fun shouldRemoveProgramThemeForTrackedEntityTypeWithNoColor() {
        val teTypeColor = "#ffcdd2"
        val themeColor = R.style.colorPrimary_Pink
        whenever(
            trackedEntityTypeConfiguration.getTrackedEntityTypeColor("uid")
        ) doReturn teTypeColor
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
            { result = false }
        )
        assertTrue(result)
    }

    @Test
    fun shouldReturnAppThemeColorIfProgramIsNull() {
        var result = false
        themeManager.getThemePrimaryColor(
            null,
            { result = false },
            { result = true }
        )
        assertTrue(result)
    }

    @Test
    fun shouldReturnAppThemeColorIfProgramColorIsNull() {
        var result = false
        whenever(programConfiguration.getProgramColor("uid")) doReturn null
        themeManager.getThemePrimaryColor(
            "uid",
            { result = false },
            { result = true }
        )
        assertTrue(result)
    }
}
