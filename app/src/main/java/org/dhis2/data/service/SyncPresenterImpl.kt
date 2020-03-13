package org.dhis2.data.service

import androidx.annotation.VisibleForTesting
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.ArrayList
import java.util.Calendar
import kotlin.math.ceil
import org.dhis2.Bindings.toSeconds
import org.dhis2.data.prefs.Preference.Companion.DATA
import org.dhis2.data.prefs.Preference.Companion.EVENT_MAX
import org.dhis2.data.prefs.Preference.Companion.EVENT_MAX_DEFAULT
import org.dhis2.data.prefs.Preference.Companion.LIMIT_BY_ORG_UNIT
import org.dhis2.data.prefs.Preference.Companion.LIMIT_BY_PROGRAM
import org.dhis2.data.prefs.Preference.Companion.META
import org.dhis2.data.prefs.Preference.Companion.TEI_MAX
import org.dhis2.data.prefs.Preference.Companion.TEI_MAX_DEFAULT
import org.dhis2.data.prefs.Preference.Companion.TIME_DAILY
import org.dhis2.data.prefs.Preference.Companion.TIME_DATA
import org.dhis2.data.prefs.Preference.Companion.TIME_META
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.utils.DateUtils
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.settings.GeneralSettings
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.android.core.settings.ProgramSettings
import org.hisp.dhis.android.core.systeminfo.DHISVersion
import timber.log.Timber

class SyncPresenterImpl(
    private val d2: D2,
    private val preferences: PreferenceProvider,
    private val workManagerController: WorkManagerController,
    private val analyticsHelper: AnalyticsHelper
) : SyncPresenter {

    override fun syncAndDownloadEvents() {

        val (eventLimit,limitByOU,limitByProgram) = getDownloadLimits()

        Completable.fromObservable(d2.eventModule().events().upload())
            .andThen(
                Completable.fromObservable(
                    d2.eventModule()
                        .eventDownloader()
                        .limit(eventLimit)
                        .limitByOrgunit(limitByOU)
                        .limitByProgram(limitByProgram)
                        .download()
                )
            ).blockingAwait()
    }

    @VisibleForTesting
    fun getDownloadLimits(): Triple<Int, Boolean, Boolean> {
        val programSettings = getProgramSetting()
        val globalProgramSettings = programSettings.globalSettings()

        val eventLimit = globalProgramSettings?.eventsDownload() ?: preferences.getInt(
            EVENT_MAX,
            EVENT_MAX_DEFAULT
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
        val globalProgramSettings = programSettings.globalSettings()

        val teiLimit =
            globalProgramSettings?.teiDownload() ?: preferences.getInt(TEI_MAX, TEI_MAX_DEFAULT)
        val limitByOU = globalProgramSettings?.settingDownload()?.let {
            it == LimitScope.PER_ORG_UNIT || it == LimitScope.PER_OU_AND_PROGRAM
        } ?: preferences.getBoolean(LIMIT_BY_ORG_UNIT, false)
        val limitByProgram = globalProgramSettings?.settingDownload()?.let {
            it == LimitScope.PER_PROGRAM || it == LimitScope.PER_OU_AND_PROGRAM
        } ?: preferences.getBoolean(LIMIT_BY_PROGRAM, false)

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
                        }
                )
                    .doOnError { Timber.d("error while downloading TEIs") }
                    .onErrorComplete()
            )
            .blockingAwait()
    }

    override fun syncAndDownloadDataValues() {
        if (!d2.dataSetModule().dataSets().blockingIsEmpty()) {
            Completable.fromObservable(d2.dataValueModule().dataValues().upload())
                .andThen(
                    Completable.fromObservable(
                        d2.dataSetModule().dataSetCompleteRegistrations().upload()
                    )
                )
                .andThen(
                    Completable.fromObservable(d2.aggregatedModule().data().download())
                ).blockingAwait()
        }
    }

    override fun syncMetadata(progressUpdate: SyncMetadataWorker.OnProgressUpdate) {
        Completable.fromObservable(
            d2.metadataModule().download()
                .doOnNext { data ->
                    progressUpdate.onProgressUpdate(ceil(data.percentage() ?: 0.0).toInt())
                }
                .doOnComplete { setUpSMS() }

        ).blockingAwait()
    }

    private fun setUpSMS() {
        val globalSettings = getSettings()

        globalSettings?.let {
            if (!globalSettings.numberSmsToSend().isNullOrEmpty()) {
                d2.smsModule().configCase().setGatewayNumber(globalSettings.numberSmsToSend())
                    .andThen(
                        if (!globalSettings.numberSmsConfirmation().isNullOrEmpty()) {
                            d2.smsModule().configCase()
                                .setConfirmationSenderNumber(globalSettings.numberSmsConfirmation())
                        } else {
                            Completable.complete()
                        }
                    ).andThen(
                        d2.smsModule().configCase().setModuleEnabled(true)
                    ).andThen(
                        d2.smsModule().configCase().refreshMetadataIds()
                    ).blockingAwait()
            }
        }
    }

    override fun uploadResources() {
        Completable.fromObservable(d2.fileResourceModule().fileResources().upload())
            .blockingAwait()
    }

    override fun downloadResources() {
        if (d2.systemInfoModule().versionManager().isGreaterThan(DHISVersion.V2_32)) {
            d2.fileResourceModule().blockingDownload()
        }
    }

    override fun syncReservedValues() {
        val maxNumberOfValuesToReserve = getSettings()?.let {
            it.reservedValues() ?: 100
        } ?: 100
        d2.trackedEntityModule().reservedValueManager()
            .blockingDownloadAllReservedValues(maxNumberOfValuesToReserve)
    }

    override fun checkSyncStatus(): Boolean {
        val eventsOk =
            d2.eventModule().events().byState().notIn(State.SYNCED).blockingGet().isEmpty()
        val teiOk = d2.trackedEntityModule().trackedEntityInstances().byState()
            .notIn(State.SYNCED, State.RELATIONSHIP).blockingGet().isEmpty()
        return eventsOk && teiOk
    }

    override fun syncGranularEvent(eventUid: String): Observable<D2Progress> {
        return d2.eventModule().events().byUid().eq(eventUid).upload()
    }

    override fun blockSyncGranularProgram(programUid: String): ListenableWorker.Result {
        Completable.fromObservable(syncGranularProgram(programUid))
            .blockingAwait()
        return if (!checkSyncProgramStatus(programUid)) {
            ListenableWorker.Result.failure()
        } else {
            ListenableWorker.Result.success()
        }
    }

    override fun blockSyncGranularTei(teiUid: String): ListenableWorker.Result {
        Completable.fromObservable(syncGranularTEI(teiUid))
            .blockingAwait()
        if (!checkSyncTEIStatus(teiUid)) {
            val trackerImportConflicts = messageTrackerImportConflict(teiUid)
            val mergeDateConflicts = ArrayList<String>()
            trackerImportConflicts?.forEach {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it.created()?.time ?: 0
                val date = DateUtils.databaseDateFormat().format(calendar.time)
                mergeDateConflicts.add(date + "/" + it.conflict())
            }

            val data = Data.Builder()
                .putStringArray("conflict", mergeDateConflicts.toTypedArray())
                .build()
            return ListenableWorker.Result.failure(data)
        }
        return ListenableWorker.Result.success()
    }

    override fun blockSyncGranularEvent(eventUid: String): ListenableWorker.Result {
        Completable.fromObservable(syncGranularEvent(eventUid))
            .blockingAwait()
        return if (!checkSyncEventStatus(eventUid)) {
            ListenableWorker.Result.failure()
        } else {
            ListenableWorker.Result.success()
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
        catOptionCombo: Array<String>
    ): ListenableWorker.Result {
        Completable.fromObservable(
            syncGranularDataValues(orgUnitUid, attrOptionCombo, periodId, catOptionCombo)
        )
            .andThen(
                Completable.fromObservable(
                    syncGranularDataSetComplete(dataSetUid, orgUnitUid, attrOptionCombo, periodId)
                )
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
                    d2.trackedEntityModule().trackedEntityInstances().byProgramUids(
                        listOf(uid)
                    ).upload()
                } else {
                    d2.eventModule().events().byProgramUid().eq(uid).upload()
                }
            }
    }

    override fun syncGranularTEI(uid: String): Observable<D2Progress> {
        return d2.trackedEntityModule().trackedEntityInstances().byUid().eq(uid).upload()
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
        catOptionCombos: Array<String>
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
        period: String
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

    override fun checkSyncEventStatus(uid: String): Boolean {
        return d2.eventModule().events()
            .byUid().eq(uid)
            .byState().notIn(State.SYNCED)
            .blockingGet().isEmpty()
    }

    override fun checkSyncTEIStatus(uid: String): Boolean {
        return d2.trackedEntityModule().trackedEntityInstances()
            .byUid().eq(uid)
            .byState().notIn(State.SYNCED, State.RELATIONSHIP)
            .blockingGet().isEmpty()
    }

    override fun checkSyncDataValueStatus(
        orgUnit: String,
        attributeOptionCombo: String,
        period: String
    ): Boolean {
        return d2.dataValueModule().dataValues().byPeriod().eq(period)
            .byOrganisationUnitUid().eq(orgUnit)
            .byAttributeOptionComboUid().eq(attributeOptionCombo)
            .byState().notIn(State.SYNCED)
            .blockingGet().isEmpty()
    }

    override fun checkSyncProgramStatus(uid: String): Boolean {
        val program = d2.programModule().programs().uid(uid).blockingGet()

        return if (program!!.programType() == ProgramType.WITH_REGISTRATION) {
            d2.trackedEntityModule().trackedEntityInstances()
                .byProgramUids(listOf(uid))
                .byState().notIn(State.SYNCED, State.RELATIONSHIP)
                .blockingGet().isEmpty()
        } else {
            d2.eventModule().events().byProgramUid().eq(uid)
                .byState().notIn(State.SYNCED)
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
            .byState().notIn(State.SYNCED)
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
                policy = ExistingWorkPolicy.REPLACE
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
                policy = ExistingWorkPolicy.REPLACE
            )

            workManagerController.syncDataForWorker(workerItem)
        }
    }

    private fun getSettings(): GeneralSettings? {
        return d2.settingModule().generalSetting().blockingGet()
    }

    private fun getProgramSetting(): ProgramSettings {
        return d2.settingModule().programSetting().blockingGet()
    }

    override fun logTimeToFinish(millisToFinish: Long, eventName: String) {
        analyticsHelper.setEvent(
            eventName,
            (millisToFinish / 60000.0).toString(),
            eventName
        )
    }
}
