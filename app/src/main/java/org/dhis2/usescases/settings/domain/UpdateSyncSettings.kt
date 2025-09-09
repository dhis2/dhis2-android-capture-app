package org.dhis2.usescases.settings.domain

import org.dhis2.commons.Constants
import org.dhis2.commons.matomo.Categories
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.hisp.dhis.android.core.settings.LimitScope

class UpdateSyncSettings(
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper,
) {
    sealed interface SyncSettings {
        data class Scope(
            val limitScope: LimitScope?,
        ) : SyncSettings

        data class EventMaxCount(
            val eventsNumber: Int?,
        ) : SyncSettings

        data class TeiMaxCount(
            val teiNumber: Int?,
        ) : SyncSettings

        data class ReservedValues(
            val reservedValuesCount: Int?,
        ) : SyncSettings

        data object Reset : SyncSettings
    }

    suspend operator fun invoke(syncSettings: SyncSettings) {
        when (syncSettings) {
            is SyncSettings.EventMaxCount -> saveEventMaxCount(syncSettings.eventsNumber)
            is SyncSettings.Scope -> saveLimitScope(syncSettings.limitScope)
            is SyncSettings.ReservedValues -> saveReservedValues(syncSettings.reservedValuesCount)
            is SyncSettings.TeiMaxCount -> saveTeiMaxCount(syncSettings.teiNumber)
            SyncSettings.Reset -> resetParameters()
        }
    }

    private suspend fun saveLimitScope(limitScope: LimitScope?) {
        val syncParam = "sync_limitScope_save"
        analyticsHelper.trackMatomoEvent(Categories.SETTINGS, syncParam, CLICK)
        settingsRepository.saveLimitScope(limitScope!!)
    }

    private suspend fun saveEventMaxCount(eventsNumber: Int?) {
        val syncParam = "sync_eventMaxCount_save"
        analyticsHelper.trackMatomoEvent(Categories.SETTINGS, syncParam, CLICK)
        settingsRepository.saveEventsToDownload(eventsNumber!!)
    }

    private suspend fun saveTeiMaxCount(teiNumber: Int?) {
        val syncParam = "sync_teiMaxCoung_save"
        analyticsHelper.trackMatomoEvent(Categories.SETTINGS, syncParam, CLICK)
        settingsRepository.saveTeiToDownload(teiNumber!!)
    }

    private suspend fun saveReservedValues(reservedValuesCount: Int?) {
        val syncParam = "sync_reservedValues_save"
        analyticsHelper.trackMatomoEvent(Categories.SETTINGS, syncParam, CLICK)
        settingsRepository.saveReservedValuesToDownload(reservedValuesCount!!)
    }

    private suspend fun resetParameters() {
        settingsRepository.saveLimitScope(LimitScope.GLOBAL)
        settingsRepository.saveEventsToDownload(Constants.EVENT_MAX_DEFAULT)
        settingsRepository.saveTeiToDownload(Constants.TEI_MAX_DEFAULT)
    }
}
