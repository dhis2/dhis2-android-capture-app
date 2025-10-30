package org.dhis2.usescases.settingsprogram

import kotlinx.coroutines.test.runTest
import org.dhis2.R
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.settingsprogram.data.SettingsProgramRepository
import org.dhis2.usescases.settingsprogram.domain.GetProgramSpecificSettings
import org.hisp.dhis.android.core.settings.DataSetSetting
import org.hisp.dhis.android.core.settings.DataSetSettings
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.android.core.settings.ProgramSetting
import org.hisp.dhis.android.core.settings.ProgramSettings
import org.hisp.dhis.android.core.settings.SynchronizationSettings
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetProgramSpecificSettingsTest {
    private val repository: SettingsProgramRepository = mock()
    private val resourceManager: ResourceManager = mock()
    private val metadataIconProvider: MetadataIconProvider = mock()
    private val getProgramSpecificSettings =
        GetProgramSpecificSettings(repository, resourceManager, metadataIconProvider)

    @Test
    fun `should return a list of specific settings`() =
        runTest {
            val trackerProgramSetting =
                ProgramSetting
                    .builder()
                    .name("Tracker Program")
                    .uid("trackerProgramUid")
                    .teiDownload(100)
                    .settingDownload(LimitScope.PER_ORG_UNIT)
                    .build()

            val eventProgramSetting =
                ProgramSetting
                    .builder()
                    .name("Event Program")
                    .uid("eventProgramUid")
                    .teiDownload(500)
                    .settingDownload(LimitScope.PER_OU_AND_PROGRAM)
                    .build()

            val dataSetProgramSetting =
                DataSetSetting
                    .builder()
                    .name("Data set")
                    .uid("dataSetUid")
                    .periodDSDownload(12)
                    .build()

            val programSpecificSettings =
                mapOf<String, ProgramSetting>(
                    "trackerProgramUid" to trackerProgramSetting,
                    "eventProgramUid" to eventProgramSetting,
                )
            val dataSetSpecificSettings =
                mapOf<String, DataSetSetting>(
                    "dataSetUid" to dataSetProgramSetting,
                )

            val programSettings: ProgramSettings =
                mock {
                    on { specificSettings() } doReturn programSpecificSettings
                }

            val datasetSettings: DataSetSettings =
                mock {
                    on { specificSettings() } doReturn dataSetSpecificSettings
                }

            val testingSettings: SynchronizationSettings =
                mock {
                    on { programSettings() } doReturn programSettings
                    on { dataSetSettings() } doReturn datasetSettings
                }

            whenever(repository.syncSettings()) doReturn testingSettings
            whenever(repository.programStyle(any())) doReturn mock()
            whenever(repository.dataSetStyle(any())) doReturn mock()
            whenever(metadataIconProvider(any())) doReturn mock()
            whenever(resourceManager.getString(R.string.events)) doReturn "Events"
            whenever(resourceManager.getString(R.string.teis)) doReturn "Teis"
            whenever(resourceManager.getString(R.string.period)) doReturn "Period"

            val result = getProgramSpecificSettings()

            assertTrue(result.size == 3)
            assertTrue(result[0].name == "Data set")
            assertTrue(result[1].name == "Event Program")
            assertTrue(result[2].name == "Tracker Program")
        }
}
