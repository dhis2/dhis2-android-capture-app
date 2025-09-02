package org.dhis2.usescases.settings.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.commons.Constants
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.settings.LimitScope
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class UpdateSyncSettingsTest {
    private lateinit var updateSyncSettings: UpdateSyncSettings
    private val settingsRepository: SettingsRepository = mock()
    private val analyticsHelper: AnalyticsHelper = mock()

    @Before
    fun setUp() {
        updateSyncSettings = UpdateSyncSettings(settingsRepository, analyticsHelper)
    }

    @Test
    fun shouldSaveLimitScope() =
        runTest {
            val limitScopeToSave = LimitScope.PER_PROGRAM
            updateSyncSettings(UpdateSyncSettings.SyncSettings.Scope(limitScopeToSave))
            verify(settingsRepository).saveLimitScope(limitScopeToSave)
        }

    @Test
    fun shouldSaveEventMaxCount() =
        runTest {
            val eventMaxCountToSave = 10
            updateSyncSettings(UpdateSyncSettings.SyncSettings.EventMaxCount(eventMaxCountToSave))
            verify(settingsRepository).saveEventsToDownload(eventMaxCountToSave)
        }

    @Test
    fun shouldSaveTeiMaxCount() =
        runTest {
            val teiMaxCountToSave = 10
            updateSyncSettings(UpdateSyncSettings.SyncSettings.TeiMaxCount(teiMaxCountToSave))
            verify(settingsRepository).saveTeiToDownload(teiMaxCountToSave)
        }

    @Test
    fun shouldSaveReservedValues() =
        runTest {
            val reservedValuesToSave = 10
            updateSyncSettings(UpdateSyncSettings.SyncSettings.ReservedValues(reservedValuesToSave))
            verify(settingsRepository).saveReservedValuesToDownload(reservedValuesToSave)
        }

    @Test
    fun shouldResetSettings() =
        runTest {
            updateSyncSettings(UpdateSyncSettings.SyncSettings.Reset)
            verify(settingsRepository).saveLimitScope(LimitScope.GLOBAL)
            verify(settingsRepository).saveEventsToDownload(Constants.EVENT_MAX_DEFAULT)
            verify(settingsRepository).saveTeiToDownload(Constants.TEI_MAX_DEFAULT)
        }
}
