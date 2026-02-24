package org.dhis2.mobile.sync.data

import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.dates.dateTimeFormat
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.providers.LAST_META_SYNC
import org.dhis2.mobile.commons.providers.LAST_META_SYNC_STATUS
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.commons.reporting.AnalyticActions
import org.dhis2.mobile.sync.model.toSyncPeriod
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.fileresource.FileResourceDomainType
import org.hisp.dhis.android.core.maintenance.D2Error
import kotlin.math.ceil
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AndroidSyncRepository(
    private val d2: D2,
    private val preferences: PreferenceProvider,
    private val analyticsHelper: AnalyticActions,
    private val domainErrorMapper: DomainErrorMapper,
    private val dispatcher: Dispatcher,
) : SyncRepository {
    private suspend fun <T> execute(block: suspend () -> Result<T>): Result<T> =
        withContext(dispatcher.io) {
            try {
                block()
            } catch (d2Error: D2Error) {
                Result.failure(domainErrorMapper.mapToDomainError(d2Error))
            }
        }

    override suspend fun currentMetadataSyncPeriod() =
        withContext(dispatcher.io) {
            d2
                .settingModule()
                .synchronizationSettings()
                .blockingGet()
                ?.metadataSync()
                ?.toSyncPeriod()
                ?: preferences
                    .getMetadataSyncPeriod()
                    .toLong()
                    .toSyncPeriod()
        }

    override suspend fun currentDataSyncPeriod() =
        withContext(dispatcher.io) {
            d2
                .settingModule()
                .synchronizationSettings()
                .blockingGet()
                ?.dataSync()
                ?.toSyncPeriod()
                ?: preferences
                    .getDataSyncPeriod()
                    .toLong()
                    .toSyncPeriod()
        }

    override suspend fun downloadFileResources(): Result<Unit> =
        execute {
            d2
                .fileResourceModule()
                .fileResourceDownloader()
                .byDomainType()
                .eq(FileResourceDomainType.ICON)
                .blockingDownload()
            Result.success(Unit)
        }

    @OptIn(ExperimentalTime::class)
    override suspend fun saveMetadataSyncState(isSuccess: Boolean) {
        preferences.setValue(
            LAST_META_SYNC,
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .format(dateTimeFormat),
        )
        preferences.setValue(LAST_META_SYNC_STATUS, isSuccess)
    }

    override suspend fun downloadMapMetadata(): Result<Unit> =
        execute {
            d2
                .mapsModule()
                .mapLayersDownloader()
                .downloadMetadata()
                .blockingAwait()
            Result.success(Unit)
        }

    override suspend fun refreshSyncSettings() =
        execute {
            d2.settingModule().synchronizationSettings().blockingDownload()
            Result.success(Unit)
        }

    override suspend fun syncMetadata(onProgressUpdate: (Int) -> Unit) =
        execute {
            d2.metadataModule().download().blockingForEach { progress ->
                onProgressUpdate(ceil(progress.percentage() ?: 0.0).toInt())
            }
            Result.success(Unit)
        }

    override suspend fun updateProjectAnalytics(): Result<Unit> =
        execute {
            d2.settingModule().generalSetting().blockingGet()?.let { globalSettings ->
                if (globalSettings.matomoID() != null && globalSettings.matomoURL() != null) {
                    analyticsHelper.updateMatomoSecondaryTracker(
                        globalSettings.matomoURL()!!,
                        globalSettings.matomoID()!!,
                    )
                }
            } ?: analyticsHelper.clearMatomoSecondaryTracker()
            Result.success(Unit)
        }

    override suspend fun setUpSMS(): Result<Unit> =
        execute {
            d2.settingModule().generalSetting().blockingGet()?.let { globalSettings ->
                globalSettings
                    .smsGateway()
                    ?.let {
                        d2
                            .smsModule()
                            .configCase()
                            .setGatewayNumber(it)
                            .blockingAwait()
                    }
                globalSettings.smsResultSender()?.let {
                    d2
                        .smsModule()
                        .configCase()
                        .setConfirmationSenderNumber(it)
                        .blockingAwait()
                }
                d2
                    .smsModule()
                    .configCase()
                    .setModuleEnabled(true)
                    .blockingAwait()
                d2
                    .smsModule()
                    .configCase()
                    .refreshMetadataIds()
                    .blockingAwait()
            }
            Result.success(Unit)
        }
}
