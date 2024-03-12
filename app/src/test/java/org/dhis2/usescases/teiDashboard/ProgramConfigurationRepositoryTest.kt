package org.dhis2.usescases.teiDashboard

import org.dhis2.usescases.teiDashboard.data.ProgramConfigurationRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.ProgramConfigurationSetting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProgramConfigurationRepositoryTest {

    private val d2: D2 = mock()
    private lateinit var programConfigurationRepository: ProgramConfigurationRepository

    @Before
    fun setUp() {
        whenever(d2.settingModule()) doReturn mock()
        whenever(d2.settingModule().appearanceSettings()) doReturn mock()
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn true

        programConfigurationRepository = ProgramConfigurationRepository(d2)
    }

    @Test
    fun shouldReturnGlobalConfiguration() {
        val globalConfiguration: ProgramConfigurationSetting = mock()
        whenever(
            d2.settingModule().appearanceSettings().getProgramConfigurationByUid("uid"),
        ) doReturn null
        whenever(
            d2.settingModule().appearanceSettings().getGlobalProgramConfigurationSetting(),
        ) doReturn globalConfiguration

        val result = programConfigurationRepository.getConfigurationByProgram("uid")

        assertEquals(result, globalConfiguration)
    }

    @Test
    fun shouldReturnSpecificConfiguration() {
        val globalConfiguration: ProgramConfigurationSetting = mock()
        val specificConfiguration: ProgramConfigurationSetting = mock()
        whenever(
            d2.settingModule().appearanceSettings().getProgramConfigurationByUid("uid"),
        ) doReturn specificConfiguration
        whenever(
            d2.settingModule().appearanceSettings().getGlobalProgramConfigurationSetting(),
        ) doReturn globalConfiguration

        val result = programConfigurationRepository.getConfigurationByProgram("uid")

        assertEquals(result, specificConfiguration)
    }

    @Test
    fun shouldReturnNullIfNoGlobalAndSpecificExists() {
        whenever(
            d2.settingModule().appearanceSettings().getProgramConfigurationByUid("uid"),
        ) doReturn null
        whenever(
            d2.settingModule().appearanceSettings().getGlobalProgramConfigurationSetting(),
        ) doReturn null

        val result = programConfigurationRepository.getConfigurationByProgram("uid")

        assertNull(result)
    }

    @Test
    fun shouldReturnNullIfAppearanceSettingsNotExist() {
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn false

        val result = programConfigurationRepository.getConfigurationByProgram("uid")

        assertNull(result)
    }
}
