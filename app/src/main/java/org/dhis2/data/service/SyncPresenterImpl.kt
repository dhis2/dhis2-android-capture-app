package org.dhis2.data.service

import androidx.annotation.VisibleForTesting
import androidx.work.Data
import androidx.work.ListenableWorker
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.program
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.prefs.Preference.Companion.EVENT_MAX
import org.dhis2.commons.prefs.Preference.Companion.EVENT_MAX_DEFAULT
import org.dhis2.commons.prefs.Preference.Companion.LIMIT_BY_ORG_UNIT
import org.dhis2.commons.prefs.Preference.Companion.LIMIT_BY_PROGRAM
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.mobile.sync.domain.SyncStatusController
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.android.core.settings.ProgramSettings
import java.util.Calendar

class SyncPresenterImpl(
    private val d2: D2,
    private val preferences: PreferenceProvider,
    private val syncRepository: SyncRepository,
    private val syncStatusController: SyncStatusController,
) : SyncPresenter {
    @VisibleForTesting
    fun getDownloadLimits(): Triple<Int, Boolean, Boolean> {
        val programSettings = getProgramSetting()

        val globalProgramSettings = programSettings?.globalSettings()

        val eventLimit =
            globalProgramSettings?.eventsDownload() ?: preferences.getInt(
                EVENT_MAX,
                EVENT_MAX_DEFAULT,
            )

        val limitByOU =
            globalProgramSettings?.settingDownload()?.let {
                it == LimitScope.PER_ORG_UNIT || it == LimitScope.PER_OU_AND_PROGRAM
            } ?: preferences.getBoolean(LIMIT_BY_ORG_UNIT, false)

        val limitByProgram =
            globalProgramSettings?.settingDownload()?.let {
                it == LimitScope.PER_PROGRAM || it == LimitScope.PER_OU_AND_PROGRAM
            } ?: preferences.getBoolean(LIMIT_BY_PROGRAM, false)

        return Triple(eventLimit, limitByOU, limitByProgram)
    }

    override fun syncGranularEvent(eventUid: String): Observable<D2Progress> {
        Completable.fromObservable(syncRepository.uploadEvent(eventUid)).blockingAwait()
        return syncRepository
            .downLoadEvent(eventUid)
            .map { it as D2Progress }
            .mergeWith(syncRepository.downloadEventFiles(eventUid))
    }

    override fun blockSyncGranularProgram(programUid: String): ListenableWorker.Result {
        Completable
            .fromObservable(syncGranularProgram(programUid))
            .blockingAwait()
        return if (!checkSyncProgramStatus(programUid)) {
            ListenableWorker.Result.failure()
        } else {
            runBlocking {
                syncStatusController.updateSingleProgramToSuccess(programUid)
            }
            ListenableWorker.Result.success()
        }
    }

    override fun blockSyncGranularTei(teiUid: String): ListenableWorker.Result {
        Completable
            .fromObservable(syncGranularTEI(teiUid))
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

                val data =
                    Data
                        .Builder()
                        .putStringArray("conflict", mergeDateConflicts.toTypedArray())
                        .build()
                ListenableWorker.Result.failure(data)
            }

            SyncResult.INCOMPLETE -> {
                val data =
                    Data
                        .Builder()
                        .putStringArray("incomplete", arrayOf("INCOMPLETE"))
                        .build()
                ListenableWorker.Result.failure(data)
            }
        }
    }

    override fun blockSyncGranularEvent(eventUid: String): ListenableWorker.Result {
        Completable
            .fromObservable(syncGranularEvent(eventUid))
            .blockingAwait()
        return when (checkSyncEventStatus(eventUid)) {
            SyncResult.SYNC -> ListenableWorker.Result.success()
            SyncResult.ERROR -> ListenableWorker.Result.failure()
            SyncResult.INCOMPLETE -> {
                val data =
                    Data
                        .Builder()
                        .putStringArray("incomplete", arrayOf("INCOMPLETE"))
                        .build()
                ListenableWorker.Result.failure(data)
            }
        }
    }

    override fun blockSyncGranularDataSet(dataSetUid: String): ListenableWorker.Result {
        Completable
            .fromObservable(syncGranularDataSet(dataSetUid))
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
        Completable
            .fromObservable(
                syncGranularDataValues(orgUnitUid, attrOptionCombo, periodId, catOptionCombo),
            ).andThen(
                Completable.fromObservable(
                    syncGranularDataSetComplete(dataSetUid, orgUnitUid, attrOptionCombo, periodId),
                ),
            ).blockingAwait()
        return if (!checkSyncDataValueStatus(orgUnitUid, attrOptionCombo, periodId)) {
            ListenableWorker.Result.failure()
        } else {
            ListenableWorker.Result.success()
        }
    }

    override fun syncGranularProgram(uid: String): Observable<D2Progress> =
        when (d2.program(uid)?.programType()) {
            null -> null
            ProgramType.WITH_REGISTRATION -> {
                Completable.fromObservable(syncRepository.uploadTrackerProgram(uid)).blockingAwait()
                syncRepository.downloadTrackerProgram(uid)
            }

            ProgramType.WITHOUT_REGISTRATION -> {
                Completable.fromObservable(syncRepository.uploadEventProgram(uid)).blockingAwait()
                syncRepository.downloadEventProgram(uid)
            }
        }?.map { it as D2Progress }
            ?.mergeWith(syncRepository.downloadProgramFiles(uid))
            ?: Observable.empty()

    override fun syncGranularTEI(uid: String): Observable<D2Progress> {
        val enrollment = d2.enrollment(uid)
        val teiUid = enrollment?.trackedEntityInstance() ?: return Observable.empty()
        val programUid = enrollment.program()
        Completable
            .fromObservable(
                syncRepository.uploadTei(teiUid, programUid),
            ).blockingAwait()
        return syncRepository
            .downloadTei(teiUid, programUid)
            .map { it as D2Progress }
            .mergeWith(
                syncRepository.downloadTeiFiles(teiUid, programUid),
            )
    }

    override fun syncGranularDataSet(uid: String): Observable<D2Progress> =
        d2
            .dataSetModule()
            .dataSetInstances()
            .byDataSetUid()
            .eq(uid)
            .get()
            .toObservable()
            .flatMapIterable { dataSets -> dataSets }
            .flatMap { dataSetReport ->
                d2
                    .dataValueModule()
                    .dataValues()
                    .byOrganisationUnitUid()
                    .eq(dataSetReport.organisationUnitUid())
                    .byPeriod()
                    .eq(dataSetReport.period())
                    .byAttributeOptionComboUid()
                    .eq(dataSetReport.attributeOptionComboUid())
                    .upload()
            }

    override fun syncGranularDataValues(
        orgUnit: String,
        attributeOptionCombo: String,
        period: String,
        catOptionCombos: Array<String>,
    ): Observable<D2Progress> =
        d2
            .dataValueModule()
            .dataValues()
            .byAttributeOptionComboUid()
            .eq(attributeOptionCombo)
            .byOrganisationUnitUid()
            .eq(orgUnit)
            .byPeriod()
            .eq(period)
            .byCategoryOptionComboUid()
            .`in`(*catOptionCombos)
            .upload()

    override fun syncGranularDataSetComplete(
        dataSetUid: String,
        orgUnit: String,
        attributeOptionCombo: String,
        period: String,
    ): Observable<D2Progress> =
        d2
            .dataSetModule()
            .dataSetCompleteRegistrations()
            .byDataSetUid()
            .eq(dataSetUid)
            .byAttributeOptionComboUid()
            .eq(attributeOptionCombo)
            .byOrganisationUnitUid()
            .eq(orgUnit)
            .byPeriod()
            .eq(period)
            .upload()

    override fun syncGranularDataSetComplete(dataSetUid: String?): Observable<D2Progress> =
        d2
            .dataSetModule()
            .dataSetCompleteRegistrations()
            .byDataSetUid()
            .eq(dataSetUid)
            .upload()

    override fun checkSyncEventStatus(uid: String): SyncResult {
        val eventsOk =
            d2
                .eventModule()
                .events()
                .byUid()
                .eq(uid)
                .byAggregatedSyncState()
                .notIn(State.SYNCED)
                .blockingGet()
                .isEmpty()

        if (eventsOk) {
            return SyncResult.SYNC
        }

        val anyEventsToPostOrToUpdate =
            d2
                .eventModule()
                .events()
                .byUid()
                .eq(uid)
                .byAggregatedSyncState()
                .`in`(State.TO_POST, State.TO_UPDATE)
                .blockingGet()
                .isNotEmpty()

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
    ): Boolean =
        d2
            .dataValueModule()
            .dataValues()
            .byPeriod()
            .eq(period)
            .byOrganisationUnitUid()
            .eq(orgUnit)
            .byAttributeOptionComboUid()
            .eq(attributeOptionCombo)
            .bySyncState()
            .notIn(State.SYNCED)
            .blockingGet()
            .isEmpty()

    override fun checkSyncProgramStatus(uid: String): Boolean {
        val program =
            d2
                .programModule()
                .programs()
                .uid(uid)
                .blockingGet()

        return if (program!!.programType() == ProgramType.WITH_REGISTRATION) {
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byProgramUids(listOf(uid))
                .byAggregatedSyncState()
                .notIn(State.SYNCED, State.RELATIONSHIP)
                .blockingGet()
                .isEmpty()
        } else {
            d2
                .eventModule()
                .events()
                .byProgramUid()
                .eq(uid)
                .byAggregatedSyncState()
                .notIn(State.SYNCED)
                .blockingGet()
                .isEmpty()
        }
    }

    override fun checkSyncDataSetStatus(uid: String): Boolean {
        val dataSetReport =
            d2
                .dataSetModule()
                .dataSetInstances()
                .byDataSetUid()
                .eq(uid)
                .one()
                .blockingGet()

        return d2
            .dataValueModule()
            .dataValues()
            .byOrganisationUnitUid()
            .eq(dataSetReport!!.organisationUnitUid())
            .byPeriod()
            .eq(dataSetReport.period())
            .byAttributeOptionComboUid()
            .eq(dataSetReport.attributeOptionComboUid())
            .bySyncState()
            .notIn(State.SYNCED)
            .blockingGet()
            .isEmpty()
    }

    override fun messageTrackerImportConflict(uid: String): List<TrackerImportConflict>? {
        var trackerImportConflicts: List<TrackerImportConflict>? =
            d2
                .importModule()
                .trackerImportConflicts()
                .byTrackedEntityInstanceUid()
                .eq(uid)
                .blockingGet()
        if (!trackerImportConflicts.isNullOrEmpty()) {
            return trackerImportConflicts
        }

        trackerImportConflicts =
            d2
                .importModule()
                .trackerImportConflicts()
                .byEventUid()
                .eq(uid)
                .blockingGet()
        if (trackerImportConflicts.isNotEmpty()) {
            return trackerImportConflicts
        }

        trackerImportConflicts =
            d2
                .importModule()
                .trackerImportConflicts()
                .byEnrollmentUid()
                .eq(uid)
                .blockingGet()
        return trackerImportConflicts.ifEmpty {
            null
        }
    }

    private fun getProgramSetting(): ProgramSettings? =
        d2
            .settingModule()
            .synchronizationSettings()
            .blockingGet()
            ?.programSettings()
}
