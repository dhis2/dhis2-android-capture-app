package org.dhis2.mobile.sync.data

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.dates.dateTimeFormat
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.providers.EVENT_MAX
import org.dhis2.mobile.commons.providers.EVENT_MAX_DEFAULT
import org.dhis2.mobile.commons.providers.LAST_DATA_SYNC
import org.dhis2.mobile.commons.providers.LAST_DATA_SYNC_STATUS
import org.dhis2.mobile.commons.providers.LAST_META_SYNC
import org.dhis2.mobile.commons.providers.LAST_META_SYNC_STATUS
import org.dhis2.mobile.commons.providers.LIMIT_BY_ORG_UNIT
import org.dhis2.mobile.commons.providers.LIMIT_BY_PROGRAM
import org.dhis2.mobile.commons.providers.MAX_RESERVED_VALUES
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.commons.providers.SYNC_RESULT
import org.dhis2.mobile.commons.providers.TEI_MAX
import org.dhis2.mobile.commons.providers.TEI_MAX_DEFAULT
import org.dhis2.mobile.commons.reporting.AnalyticActions
import org.dhis2.mobile.sync.model.DataSyncProgressStatus
import org.dhis2.mobile.sync.model.SyncResult
import org.dhis2.mobile.sync.model.toSyncPeriod
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2ProgressStatus
import org.hisp.dhis.android.core.arch.call.D2ProgressSyncStatus
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.fileresource.FileResourceDomainType
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.settings.LimitScope
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

    private suspend fun <T> run(block: suspend () -> Result<T>): Result<T> =
        try {
            block()
        } catch (d2Error: D2Error) {
            Result.failure(domainErrorMapper.mapToDomainError(d2Error))
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

    @OptIn(ExperimentalTime::class)
    override suspend fun saveDataSyncState(isSuccess: Boolean) =
        run {
            preferences.setValue(
                LAST_DATA_SYNC,
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .format(dateTimeFormat),
            )
            val syncResult = syncResult()
            preferences.setValue(
                LAST_DATA_SYNC_STATUS,
                isSuccess && syncResult == SyncResult.Sync,
            )
            preferences.setValue(
                SYNC_RESULT,
                syncResult.name,
            )
            Result.success(Unit)
        }

    override suspend fun getAllProgramsInitialStatus() =
        run {
            val programMap =
                d2
                    .programModule()
                    .programs()
                    .blockingGetUids()
                    .associateWith {
                        DataSyncProgressStatus.None
                    }
            val aggregateMap =
                d2.dataSetModule().dataSets().blockingGetUids().associateWith {
                    DataSyncProgressStatus.None
                }

            Result.success(programMap + aggregateMap)
        }

    override suspend fun getAllEventPrograms() =
        run {
            val uids =
                d2
                    .programModule()
                    .programs()
                    .byProgramType()
                    .eq(ProgramType.WITHOUT_REGISTRATION)
                    .blockingGetUids()
            Result.success(uids)
        }

    override suspend fun getAllTrackerPrograms() =
        run {
            val uids =
                d2
                    .programModule()
                    .programs()
                    .byProgramType()
                    .eq(ProgramType.WITH_REGISTRATION)
                    .blockingGetUids()
            Result.success(uids)
        }

    override suspend fun getAllDataSets() =
        run {
            val uids =
                d2
                    .dataSetModule()
                    .dataSets()
                    .blockingGetUids()
            Result.success(uids)
        }

    private suspend fun syncResult(): SyncResult {
        val eventsOk =
            d2
                .eventModule()
                .events()
                .byAggregatedSyncState()
                .notIn(State.SYNCED)
                .blockingGet()
                .isEmpty()
        val teiOk =
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .notIn(
                    State.SYNCED,
                    State.RELATIONSHIP,
                ).blockingGet()
                .isEmpty()

        if (eventsOk && teiOk) {
            return SyncResult.Sync
        }

        val anyEventsToPostOrToUpdate =
            d2
                .eventModule()
                .events()
                .byAggregatedSyncState()
                .`in`(
                    State.TO_POST,
                    State.TO_UPDATE,
                ).blockingGet()
                .isNotEmpty()
        val anyTeiToPostOrToUpdate =
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byAggregatedSyncState()
                .`in`(State.TO_POST, State.TO_UPDATE)
                .blockingGet()
                .isNotEmpty()

        if (anyEventsToPostOrToUpdate || anyTeiToPostOrToUpdate) {
            return SyncResult.Incomplete
        }

        return SyncResult.Error
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

    override suspend fun uploadEvents(): Result<Unit> =
        run {
            d2.eventModule().events().blockingUpload()
            Result.success(Unit)
        }

    override suspend fun uploadTEIs(): Result<Unit> =
        run {
            d2.trackedEntityModule().trackedEntityInstances().blockingUpload()
            Result.success(Unit)
        }

    override suspend fun uploadDataValues(): Result<Unit> =
        run {
            d2.dataValueModule().dataValues().blockingUpload()
            d2.dataSetModule().dataSetCompleteRegistrations().blockingUpload()
            Result.success(Unit)
        }

    override suspend fun downloadEvents(
        onProgressUpdate: suspend (progressData: Map<String, DataSyncProgressStatus>) -> Unit,
    ): Result<Unit> =
        run {
            val eventProgramUids =
                d2
                    .programModule()
                    .programs()
                    .byProgramType()
                    .eq(ProgramType.WITHOUT_REGISTRATION)
                    .blockingGetUids()

            val globalSettings =
                d2
                    .settingModule()
                    .synchronizationSettings()
                    .blockingGet()
                    ?.programSettings()
                    ?.globalSettings()

            val eventLimit =
                globalSettings?.eventsDownload() ?: preferences.getInt(
                    EVENT_MAX,
                    EVENT_MAX_DEFAULT,
                )

            val limitByOrgUnit =
                globalSettings?.settingDownload()?.let {
                    it == LimitScope.PER_ORG_UNIT || it == LimitScope.PER_OU_AND_PROGRAM
                } ?: preferences.getBoolean(
                    LIMIT_BY_ORG_UNIT,
                    false,
                )

            val limitByProgram =
                globalSettings?.settingDownload()?.let {
                    it == LimitScope.PER_PROGRAM || it == LimitScope.PER_OU_AND_PROGRAM
                } ?: preferences.getBoolean(
                    LIMIT_BY_PROGRAM,
                    false,
                )

            d2
                .eventModule()
                .eventDownloader()
                .limit(eventLimit)
                .limitByOrgunit(limitByOrgUnit)
                .limitByProgram(limitByProgram)
                .download()
                .blockingForEach { progress ->
                    val progressData =
                        progress
                            .programs()
                            .filter { entry ->
                                eventProgramUids.contains(entry.key)
                            }.mapValues { (_, programProgress) ->
                                programProgress.toSycStatus()
                            }
                    runBlocking {
                        onProgressUpdate(progressData)
                    }
                }

            Result.success(Unit)
        }

    override suspend fun downloadTEIs(onProgressUpdate: suspend (progressData: Map<String, DataSyncProgressStatus>) -> Unit): Result<Unit> =
        run {
            val trackerProgramUids =
                d2
                    .programModule()
                    .programs()
                    .byProgramType()
                    .eq(ProgramType.WITH_REGISTRATION)
                    .blockingGetUids()

            val globalSettings =
                d2
                    .settingModule()
                    .synchronizationSettings()
                    .blockingGet()
                    ?.programSettings()
                    ?.globalSettings()

            val teiLimit =
                globalSettings?.teiDownload() ?: preferences.getInt(
                    TEI_MAX,
                    TEI_MAX_DEFAULT,
                )

            val limitByOrgUnit =
                globalSettings?.settingDownload()?.let {
                    it == LimitScope.PER_ORG_UNIT || it == LimitScope.PER_OU_AND_PROGRAM
                } ?: preferences.getBoolean(
                    LIMIT_BY_ORG_UNIT,
                    false,
                )

            val limitByProgram =
                globalSettings?.settingDownload()?.let {
                    it == LimitScope.PER_PROGRAM || it == LimitScope.PER_OU_AND_PROGRAM
                } ?: preferences.getBoolean(
                    LIMIT_BY_PROGRAM,
                    false,
                )

            d2
                .trackedEntityModule()
                .trackedEntityInstanceDownloader()
                .limit(teiLimit)
                .limitByOrgunit(limitByOrgUnit)
                .limitByProgram(limitByProgram)
                .download()
                .blockingForEach { progress ->
                    val progressData =
                        progress
                            .programs()
                            .filter { entry ->
                                trackerProgramUids.contains(entry.key)
                            }.mapValues { (_, programProgress) ->
                                programProgress.toSycStatus()
                            }
                    runBlocking {
                        onProgressUpdate(progressData)
                    }
                }
            Result.success(Unit)
        }

    override suspend fun downloadDataValues(onProgressUpdate: (progressData: Map<String, DataSyncProgressStatus>) -> Unit): Result<Unit> =
        run {
            d2
                .aggregatedModule()
                .data()
                .download()
                .blockingForEach { progress ->
                    val progressData =
                        progress.dataSets().mapValues { (_, dataSetProgress) ->
                            dataSetProgress.toSycStatus()
                        }
                    onProgressUpdate(progressData)
                }
            Result.success(Unit)
        }

    override suspend fun downloadDataFileResources(onProgressUpdate: (progress: Double?) -> Unit): Result<Unit> =
        run {
            d2
                .fileResourceModule()
                .fileResourceDownloader()
                .byDomainType()
                .eq(FileResourceDomainType.DATA_VALUE)
                .download()
                .blockingForEach { progress ->
                    onProgressUpdate(progress.percentage())
                }
            Result.success(Unit)
        }

    override suspend fun downloadReservedValues(onProgressUpdate: (progress: Double?) -> Unit): Result<Unit> =
        run {
            val maxNumberOfValuesToFillUp =
                d2
                    .settingModule()
                    .generalSetting()
                    .blockingGet()
                    ?.reservedValues() ?: MAX_RESERVED_VALUES

            d2
                .trackedEntityModule()
                .reservedValueManager()
                .downloadAllReservedValues(maxNumberOfValuesToFillUp)
                .blockingForEach { progress ->
                    onProgressUpdate(progress.percentage())
                }
            Result.success(Unit)
        }

    private fun D2ProgressStatus.toSycStatus() =
        when {
            isComplete.not() -> DataSyncProgressStatus.InProgress
            syncStatus == D2ProgressSyncStatus.SUCCESS -> DataSyncProgressStatus.Success
            syncStatus == D2ProgressSyncStatus.ERROR -> DataSyncProgressStatus.Failed
            syncStatus == D2ProgressSyncStatus.PARTIAL_ERROR -> DataSyncProgressStatus.PartiallyFailed
            else -> DataSyncProgressStatus.None
        }
}
