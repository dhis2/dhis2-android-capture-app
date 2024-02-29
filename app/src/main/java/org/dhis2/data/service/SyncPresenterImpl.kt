package org.dhis2.data.service

import androidx.annotation.VisibleForTesting
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import io.reactivex.Completable
import io.reactivex.Observable
import org.dhis2.bindings.toSeconds
import org.dhis2.commons.prefs.Preference.Companion.DATA
import org.dhis2.commons.prefs.Preference.Companion.EVENT_MAX
import org.dhis2.commons.prefs.Preference.Companion.EVENT_MAX_DEFAULT
import org.dhis2.commons.prefs.Preference.Companion.LIMIT_BY_ORG_UNIT
import org.dhis2.commons.prefs.Preference.Companion.LIMIT_BY_PROGRAM
import org.dhis2.commons.prefs.Preference.Companion.META
import org.dhis2.commons.prefs.Preference.Companion.TEI_MAX
import org.dhis2.commons.prefs.Preference.Companion.TEI_MAX_DEFAULT
import org.dhis2.commons.prefs.Preference.Companion.TIME_DAILY
import org.dhis2.commons.prefs.Preference.Companion.TIME_DATA
import org.dhis2.commons.prefs.Preference.Companion.TIME_META
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.utils.DateUtils
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.matomo.DEFAULT_EXTERNAL_TRACKER_NAME
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.arch.call.D2ProgressStatus
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.fileresource.FileResourceDomainType
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.settings.GeneralSettings
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.android.core.settings.ProgramSettings
import org.hisp.dhis.android.core.systeminfo.DHISVersion
import org.hisp.dhis.android.core.tracker.exporter.TrackerD2Progress
import timber.log.Timber
import java.util.Calendar
import kotlin.math.ceil

class SyncPresenterImpl(
    private val d2: D2,
    private val preferences: PreferenceProvider,
    private val workManagerController: WorkManagerController,
    private val analyticsHelper: AnalyticsHelper,
    private val syncStatusController: SyncStatusController,
    private val syncRepository: SyncRepository,
) : SyncPresenter {

    override fun initSyncControllerMap() {
        Completable.fromCallable {
            val programMap: Map<String, D2ProgressStatus> =
                d2.programModule().programs().blockingGetUids().map { programUid ->
                    programUid to D2ProgressStatus(false, null)
                }.toMap()
            val aggregateMap: Map<String, D2ProgressStatus> =
                d2.dataSetModule().dataSets().blockingGetUids().associateWith {
                    D2ProgressStatus(false, null)
                }
            val allMap = programMap.toMutableMap().apply {
                putAll(aggregateMap)
            }.toMap()
            syncStatusController.initDownloadProcess(allMap)
        }.blockingAwait()
    }

    override fun finishSync() {
        syncStatusController.finishSync()
    }

    override fun setNetworkUnavailable() {
        syncStatusController.onNetworkUnavailable()
    }

    override fun syncAndDownloadEvents() {
        val (eventLimit, limitByOU, limitByProgram) = getDownloadLimits()
        val programEventUids = d2.programModule().programs()
            .byProgramType().eq(ProgramType.WITHOUT_REGISTRATION)
            .blockingGetUids()
        Completable.fromObservable(d2.eventModule().events().upload())
            .andThen(
                Completable.fromObservable(
                    d2.eventModule()
                        .eventDownloader()
                        .limit(eventLimit)
                        .limitByOrgunit(limitByOU)
                        .limitByProgram(limitByProgram)
                        .download()
                        .doOnNext { d2Progress ->
                            syncStatusController.updateDownloadProcess(
                                d2Progress.programs().filter { entry ->
                                    programEventUids.contains(entry.key)
                                },
                            )
                        },
                )
                    .doOnError {
                        Timber.d("error while downloading Events")
                    }
                    .onErrorComplete()
                    .doOnComplete {
                        syncStatusController.finishDownloadingEvents(
                            programEventUids,
                        )
                    },
            ).blockingAwait()
    }

    @VisibleForTesting
    fun getDownloadLimits(): Triple<Int, Boolean, Boolean> {
        val programSettings = getProgramSetting()

        val globalProgramSettings = programSettings?.globalSettings()

        val eventLimit = globalProgramSettings?.eventsDownload() ?: preferences.getInt(
            EVENT_MAX,
            EVENT_MAX_DEFAULT,
        )

        val limitByOU = globalProgramSettings?.settingDownload()?.let {
            it == LimitScope.PER_ORG_UNIT || it == LimitScope.PER_OU_AND_PROGRAM
        } ?: preferences.getBoolean(LIMIT_BY_ORG_UNIT, false)

        val limitByProgram = globalProgramSettings?.settingDownload()?.let {
            it == LimitScope.PER_PROGRAM || it == LimitScope.PER_OU_AND_PROGRAM
        } ?: preferences.getBoolean(LIMIT_BY_PROGRAM, false)

        return Triple(eventLimit, limitByOU, limitByProgram)
    }

    override fun syncAndDownloadTeis() {
        val programSettings = getProgramSetting()
        val globalProgramSettings = programSettings?.globalSettings()

        val teiLimit =
            globalProgramSettings?.teiDownload() ?: preferences.getInt(TEI_MAX, TEI_MAX_DEFAULT)
        val limitByOU = globalProgramSettings?.settingDownload()?.let {
            it == LimitScope.PER_ORG_UNIT || it == LimitScope.PER_OU_AND_PROGRAM
        } ?: preferences.getBoolean(LIMIT_BY_ORG_UNIT, false)
        val limitByProgram = globalProgramSettings?.settingDownload()?.let {
            it == LimitScope.PER_PROGRAM || it == LimitScope.PER_OU_AND_PROGRAM
        } ?: preferences.getBoolean(LIMIT_BY_PROGRAM, false)

        val trackerProgramUids = d2.programModule().programs()
            .byProgramType().eq(ProgramType.WITH_REGISTRATION)
            .blockingGetUids()

        Completable.fromObservable(d2.trackedEntityModule().trackedEntityInstances().upload())
            .andThen(
                Completable.fromObservable(
                    d2.trackedEntityModule()
                        .trackedEntityInstanceDownloader()
                        .limit(teiLimit)
                        .limitByOrgunit(limitByOU)
                        .limitByProgram(limitByProgram)
                        .download()
                        .doOnNext { data ->
                            val percentage = data.percentage()
                            val callsDone = data.doneCalls().size
                            val totalCalls = data.totalCalls()
                            Timber.d("$percentage% $callsDone/$totalCalls")
                            syncStatusController.updateDownloadProcess(
                                data.programs().filter { entry ->
                                    trackerProgramUids.contains(entry.key)
                                },
                            )
                        },
                )
                    .doOnError { Timber.d("error while downloading TEIs") }
                    .onErrorComplete()
                    .doOnComplete {
                        syncStatusController.finishDownloadingTracker(
                            trackerProgramUids,
                        )
                    },
            )
            .blockingAwait()
    }

    override fun syncAndDownloadDataValues() {
        if (!d2.dataSetModule().dataSets().blockingIsEmpty()) {
            Completable.fromObservable(d2.dataValueModule().dataValues().upload())
                .andThen(
                    Completable.fromObservable(
                        d2.dataSetModule().dataSetCompleteRegistrations().upload(),
                    ),
                )
                .andThen(
                    Completable.fromObservable(
                        d2.aggregatedModule().data().download().doOnNext {
                            syncStatusController.updateDownloadProcess(it.dataSets())
                        },
                    ),
                ).blockingAwait()
        }
    }

    override fun syncMetadata(progressUpdate: SyncMetadataWorker.OnProgressUpdate) {
        Completable.fromObservable(
            d2.metadataModule().download()
                .doOnNext { data ->
                    Timber.log(1, data.toString())
                    progressUpdate.onProgressUpdate(ceil(data.percentage() ?: 0.0).toInt())
                }
                .doOnComplete {
                    updateProyectAnalytics()
                    setUpSMS()
                },

        ).andThen(
            d2.mapsModule().mapLayersDownloader().downloadMetadata(),
        ).andThen(
            Completable.fromObservable(
                d2.fileResourceModule().fileResourceDownloader()
                    .byDomainType().eq(FileResourceDomainType.CUSTOM_ICON)
                    .download(),
            ),
        ).blockingAwait()
    }

    private fun setUpSMS() {
        val globalSettings = getSettings()

        globalSettings?.let {
            if (!globalSettings.smsGateway().isNullOrEmpty()) {
                d2.smsModule().configCase().setGatewayNumber(globalSettings.smsGateway())
                    .andThen(
                        if (!globalSettings.smsResultSender().isNullOrEmpty()) {
                            d2.smsModule().configCase()
                                .setConfirmationSenderNumber(globalSettings.smsResultSender())
                        } else {
                            Completable.complete()
                        },
                    ).andThen(
                        d2.smsModule().configCase().setModuleEnabled(true),
                    ).andThen(
                        d2.smsModule().configCase().refreshMetadataIds(),
                    ).blockingAwait()
            }
        }
    }

    override fun downloadResources() {
        if (d2.systemInfoModule().versionManager().isGreaterThan(DHISVersion.V2_32)) {
            syncStatusController.initDownloadMedia()
            Completable.fromObservable(
                d2.fileResourceModule().fileResourceDownloader()
                    .byDomainType().eq(FileResourceDomainType.DATA_VALUE)
                    .download(),
            ).blockingAwait()
        }
    }

    override fun syncReservedValues() {
        val maxNumberOfValuesToReserve = getSettings()?.let {
            it.reservedValues() ?: 100
        } ?: 100
        Completable.fromObservable(
            d2.trackedEntityModule().reservedValueManager()
                .downloadAllReservedValues(maxNumberOfValuesToReserve),
        ).blockingAwait()
    }

    override fun checkSyncStatus(): SyncResult {
        val eventsOk = d2.eventModule().events()
            .byAggregatedSyncState().notIn(State.SYNCED).blockingGet().isEmpty()
        val teiOk = d2.trackedEntityModule().trackedEntityInstances().byAggregatedSyncState()
            .notIn(State.SYNCED, State.RELATIONSHIP).blockingGet().isEmpty()

        if (eventsOk && teiOk) {
            return SyncResult.SYNC
        }

        val anyEventsToPostOrToUpdate = d2.eventModule()
            .events()
            .byAggregatedSyncState().`in`(State.TO_POST, State.TO_UPDATE)
            .blockingGet().isNotEmpty()
        val anyTeiToPostOrToUpdate = d2.trackedEntityModule()
            .trackedEntityInstances()
            .byAggregatedSyncState().`in`(State.TO_POST, State.TO_UPDATE)
            .blockingGet().isNotEmpty()

        if (anyEventsToPostOrToUpdate || anyTeiToPostOrToUpdate) {
            return SyncResult.INCOMPLETE
        }

        return SyncResult.ERROR
    }

    override fun syncGranularEvent(eventUid: String): Observable<TrackerD2Progress> {
        Completable.fromObservable(d2.eventModule().events().byUid().eq(eventUid).upload())
            .blockingAwait()
        return d2.eventModule().eventDownloader().byUid().eq(eventUid).download()
    }

    override fun blockSyncGranularProgram(programUid: String): ListenableWorker.Result {
        Completable.fromObservable(syncGranularProgram(programUid))
            .blockingAwait()
        return if (!checkSyncProgramStatus(programUid)) {
            ListenableWorker.Result.failure()
        } else {
            syncStatusController.updateSingleProgramToSuccess(programUid)
            ListenableWorker.Result.success()
        }
    }

    override fun blockSyncGranularTei(teiUid: String): ListenableWorker.Result {
        Completable.fromObservable(syncGranularTEI(teiUid))
            .blockingAwait()
        return when (checkSyncTEIStatus(teiUid)) {
            SyncResult.SYNC -> {
                ListenableWorker.Result.success()
            }

            SyncResult.ERROR -> {
                val trackerImportConflicts = messageTrackerImportConflict(teiUid)
                val mergeDateConflicts = ArrayList<String>()
                trackerImportConflicts?.forEach {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = it.created()?.time ?: 0
                    val date = DateUtils.databaseDateFormat().format(calendar.time)
                    mergeDateConflicts.add(date + "/" + it.displayDescription())
                }

                val data = Data.Builder()
                    .putStringArray("conflict", mergeDateConflicts.toTypedArray())
                    .build()
                ListenableWorker.Result.failure(data)
            }

            SyncResult.INCOMPLETE -> {
                val data = Data.Builder()
                    .putStringArray("incomplete", arrayOf("INCOMPLETE"))
                    .build()
                ListenableWorker.Result.failure(data)
            }
        }
    }

    override fun blockSyncGranularEvent(eventUid: String): ListenableWorker.Result {
        Completable.fromObservable(syncGranularEvent(eventUid))
            .blockingAwait()
        return when (checkSyncEventStatus(eventUid)) {
            SyncResult.SYNC -> ListenableWorker.Result.success()
            SyncResult.ERROR -> ListenableWorker.Result.failure()
            SyncResult.INCOMPLETE -> {
                val data = Data.Builder()
                    .putStringArray("incomplete", arrayOf("INCOMPLETE"))
                    .build()
                ListenableWorker.Result.failure(data)
            }
        }
    }

    override fun blockSyncGranularDataSet(dataSetUid: String): ListenableWorker.Result {
        Completable.fromObservable(syncGranularDataSet(dataSetUid))
            .andThen(Completable.fromObservable(syncGranularDataSetComplete(dataSetUid)))
            .blockingAwait()
        return if (!checkSyncDataSetStatus(dataSetUid)) {
            ListenableWorker.Result.failure()
        } else {
            ListenableWorker.Result.success()
        }
    }

    override fun blockSyncGranularDataValues(
        dataSetUid: String,
        orgUnitUid: String,
        attrOptionCombo: String,
        periodId: String,
        catOptionCombo: Array<String>,
    ): ListenableWorker.Result {
        Completable.fromObservable(
            syncGranularDataValues(orgUnitUid, attrOptionCombo, periodId, catOptionCombo),
        )
            .andThen(
                Completable.fromObservable(
                    syncGranularDataSetComplete(dataSetUid, orgUnitUid, attrOptionCombo, periodId),
                ),
            )
            .blockingAwait()
        return if (!checkSyncDataValueStatus(orgUnitUid, attrOptionCombo, periodId)) {
            ListenableWorker.Result.failure()
        } else {
            ListenableWorker.Result.success()
        }
    }

    override fun syncGranularProgram(uid: String): Observable<D2Progress> {
        return d2.programModule().programs().uid(uid).get().toObservable()
            .flatMap { program ->
                if (program.programType() == ProgramType.WITH_REGISTRATION) {
                    Completable.fromObservable(
                        d2.trackedEntityModule().trackedEntityInstances().byProgramUids(listOf(uid))
                            .upload(),
                    ).blockingAwait()

                    d2.trackedEntityModule().trackedEntityInstanceDownloader().byProgramUid(uid)
                        .download()
                } else {
                    Completable.fromObservable(
                        d2.eventModule().events().byProgramUid().eq(uid).upload(),
                    ).blockingAwait()
                    d2.eventModule().eventDownloader().byProgramUid(uid).download()
                }
            }
    }

    override fun syncGranularTEI(uid: String): Observable<TrackerD2Progress> {
        val enrollment = d2.enrollmentModule().enrollments().uid(uid).blockingGet()
        Completable.fromObservable(
            d2.trackedEntityModule().trackedEntityInstances()
                .byUid().eq(enrollment?.trackedEntityInstance())
                .byProgramUids(enrollment?.program()?.let { listOf(it) } ?: emptyList())
                .upload(),
        ).blockingAwait()
        return d2.trackedEntityModule().trackedEntityInstanceDownloader()
            .byUid().eq(enrollment?.trackedEntityInstance())
            .byProgramUid(enrollment?.program() ?: "")
            .download()
    }

    override fun syncGranularDataSet(uid: String): Observable<D2Progress> {
        return d2.dataSetModule().dataSetInstances().byDataSetUid().eq(uid).get().toObservable()
            .flatMapIterable { dataSets -> dataSets }
            .flatMap { dataSetReport ->
                d2.dataValueModule().dataValues()
                    .byOrganisationUnitUid().eq(dataSetReport.organisationUnitUid())
                    .byPeriod().eq(dataSetReport.period())
                    .byAttributeOptionComboUid().eq(dataSetReport.attributeOptionComboUid())
                    .upload()
            }
    }

    override fun syncGranularDataValues(
        orgUnit: String,
        attributeOptionCombo: String,
        period: String,
        catOptionCombos: Array<String>,
    ): Observable<D2Progress> {
        return d2.dataValueModule().dataValues()
            .byAttributeOptionComboUid().eq(attributeOptionCombo)
            .byOrganisationUnitUid().eq(orgUnit)
            .byPeriod().eq(period)
            .byCategoryOptionComboUid().`in`(*catOptionCombos)
            .upload()
    }

    override fun syncGranularDataSetComplete(
        dataSetUid: String,
        orgUnit: String,
        attributeOptionCombo: String,
        period: String,
    ): Observable<D2Progress> {
        return d2.dataSetModule().dataSetCompleteRegistrations()
            .byDataSetUid().eq(dataSetUid)
            .byAttributeOptionComboUid().eq(attributeOptionCombo)
            .byOrganisationUnitUid().eq(orgUnit)
            .byPeriod().eq(period).upload()
    }

    override fun syncGranularDataSetComplete(dataSetUid: String?): Observable<D2Progress> {
        return d2.dataSetModule().dataSetCompleteRegistrations()
            .byDataSetUid().eq(dataSetUid)
            .upload()
    }

    override fun checkSyncEventStatus(uid: String): SyncResult {
        val eventsOk = d2.eventModule().events()
            .byUid().eq(uid)
            .byAggregatedSyncState().notIn(State.SYNCED)
            .blockingGet().isEmpty()

        if (eventsOk) {
            return SyncResult.SYNC
        }

        val anyEventsToPostOrToUpdate = d2.eventModule()
            .events()
            .byUid().eq(uid)
            .byAggregatedSyncState().`in`(State.TO_POST, State.TO_UPDATE)
            .blockingGet().isNotEmpty()

        if (anyEventsToPostOrToUpdate) {
            return SyncResult.INCOMPLETE
        }

        return SyncResult.ERROR
    }

    override fun checkSyncTEIStatus(uid: String): SyncResult {
        val teiOk =
            syncRepository.getTeiByNotInStates(uid, listOf(State.SYNCED, State.RELATIONSHIP))
        val teiEventsOk =
            syncRepository.getEventsFromEnrollmentByNotInSyncState(uid, listOf(State.SYNCED))

        if (teiOk.isEmpty() && teiEventsOk.isEmpty()) {
            return SyncResult.SYNC
        }

        val anyTeiToPostOrToUpdate =
            syncRepository.getTeiByInStates(uid, listOf(State.TO_POST, State.TO_UPDATE))

        if (anyTeiToPostOrToUpdate.isNotEmpty()) {
            return SyncResult.INCOMPLETE
        }

        return SyncResult.ERROR
    }

    override fun checkSyncDataValueStatus(
        orgUnit: String,
        attributeOptionCombo: String,
        period: String,
    ): Boolean {
        return d2.dataValueModule().dataValues().byPeriod().eq(period)
            .byOrganisationUnitUid().eq(orgUnit)
            .byAttributeOptionComboUid().eq(attributeOptionCombo)
            .bySyncState().notIn(State.SYNCED)
            .blockingGet().isEmpty()
    }

    override fun checkSyncProgramStatus(uid: String): Boolean {
        val program = d2.programModule().programs().uid(uid).blockingGet()

        return if (program!!.programType() == ProgramType.WITH_REGISTRATION) {
            d2.trackedEntityModule().trackedEntityInstances()
                .byProgramUids(listOf(uid))
                .byAggregatedSyncState().notIn(State.SYNCED, State.RELATIONSHIP)
                .blockingGet().isEmpty()
        } else {
            d2.eventModule().events().byProgramUid().eq(uid)
                .byAggregatedSyncState().notIn(State.SYNCED)
                .blockingGet().isEmpty()
        }
    }

    override fun checkSyncDataSetStatus(uid: String): Boolean {
        val dataSetReport =
            d2.dataSetModule().dataSetInstances().byDataSetUid().eq(uid).one().blockingGet()

        return d2.dataValueModule().dataValues()
            .byOrganisationUnitUid().eq(dataSetReport!!.organisationUnitUid())
            .byPeriod().eq(dataSetReport.period())
            .byAttributeOptionComboUid().eq(dataSetReport.attributeOptionComboUid())
            .bySyncState().notIn(State.SYNCED)
            .blockingGet().isEmpty()
    }

    override fun messageTrackerImportConflict(uid: String): List<TrackerImportConflict>? {
        var trackerImportConflicts: List<TrackerImportConflict>? =
            d2.importModule().trackerImportConflicts().byTrackedEntityInstanceUid().eq(uid)
                .blockingGet()
        if (trackerImportConflicts != null && trackerImportConflicts.isNotEmpty()) {
            return trackerImportConflicts
        }

        trackerImportConflicts =
            d2.importModule().trackerImportConflicts().byEventUid().eq(uid).blockingGet()
        if (trackerImportConflicts != null && trackerImportConflicts.isNotEmpty()) {
            return trackerImportConflicts
        }

        trackerImportConflicts =
            d2.importModule().trackerImportConflicts().byEnrollmentUid().eq(uid).blockingGet()
        return if (trackerImportConflicts != null && trackerImportConflicts.isNotEmpty()) {
            trackerImportConflicts
        } else {
            null
        }
    }

    override fun startPeriodicDataWork() {
        val seconds =
            getSettings()?.dataSync()?.toSeconds() ?: preferences.getInt(TIME_DATA, TIME_DAILY)
        workManagerController.cancelUniqueWork(DATA)

        if (seconds != 0) {
            val workerItem = WorkerItem(
                DATA,
                WorkerType.DATA,
                seconds.toLong(),
                policy = ExistingWorkPolicy.REPLACE,
            )

            workManagerController.syncDataForWorker(workerItem)
        }
    }

    override fun startPeriodicMetaWork() {
        val seconds =
            getSettings()?.metadataSync()?.toSeconds() ?: preferences.getInt(TIME_META, TIME_DAILY)
        workManagerController.cancelUniqueWork(META)

        if (seconds != 0) {
            val workerItem = WorkerItem(
                META,
                WorkerType.METADATA,
                seconds.toLong(),
                policy = ExistingWorkPolicy.REPLACE,
            )

            workManagerController.syncDataForWorker(workerItem)
        }
    }

    private fun getSettings(): GeneralSettings? {
        return d2.settingModule().generalSetting().blockingGet()
    }

    private fun getProgramSetting(): ProgramSettings? {
        return d2.settingModule().programSetting().blockingGet()
    }

    override fun logTimeToFinish(millisToFinish: Long, eventName: String) {
        analyticsHelper.setEvent(
            eventName,
            (millisToFinish / 60000.0).toString(),
            eventName,
        )
    }

    override fun updateProyectAnalytics() {
        getSettings()?.let {
            if (it.matomoID() != null && it.matomoURL() != null) {
                analyticsHelper.updateMatomoSecondaryTracker(
                    it.matomoURL()!!,
                    it.matomoID()!!,
                    DEFAULT_EXTERNAL_TRACKER_NAME,
                )
            }
        } ?: analyticsHelper.clearMatomoSecondaryTracker()
    }
}
