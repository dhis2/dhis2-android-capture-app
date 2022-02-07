/*
 * Copyright (c) 2004 - 2019, University of Oslo
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.utils.granularsync

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.schedulers.Schedulers
import java.util.Date
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.sms.SmsSendingService
import org.dhis2.utils.Constants
import org.dhis2.utils.Constants.ATTRIBUTE_OPTION_COMBO
import org.dhis2.utils.Constants.CATEGORY_OPTION_COMBO
import org.dhis2.utils.Constants.CONFLICT_TYPE
import org.dhis2.utils.Constants.ORG_UNIT
import org.dhis2.utils.Constants.PERIOD_ID
import org.dhis2.utils.Constants.UID
import org.dhis2.utils.granularsync.SyncStatusDialog.ConflictType.ALL
import org.dhis2.utils.granularsync.SyncStatusDialog.ConflictType.DATA_SET
import org.dhis2.utils.granularsync.SyncStatusDialog.ConflictType.DATA_VALUES
import org.dhis2.utils.granularsync.SyncStatusDialog.ConflictType.EVENT
import org.dhis2.utils.granularsync.SyncStatusDialog.ConflictType.PROGRAM
import org.dhis2.utils.granularsync.SyncStatusDialog.ConflictType.TEI
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase
import org.hisp.dhis.android.core.sms.domain.repository.SmsRepository
import org.hisp.dhis.android.core.systeminfo.SMSVersion
import timber.log.Timber

class GranularSyncPresenterImpl(
    val d2: D2,
    private val dhisProgramUtils: DhisProgramUtils,
    val schedulerProvider: SchedulerProvider,
    private val conflictType: SyncStatusDialog.ConflictType,
    private val recordUid: String,
    private val dvOrgUnit: String?,
    private val dvAttrCombo: String?,
    private val dvPeriodId: String?,
    private val workManagerController: WorkManagerController,
    private val errorMapper: ErrorModelMapper,
    private val preferenceProvider: PreferenceProvider
) : GranularSyncContracts.Presenter {

    private var disposable: CompositeDisposable = CompositeDisposable()
    private lateinit var view: GranularSyncContracts.View
    private lateinit var states: MutableLiveData<List<SmsSendingService.SendingStatus>>
    private lateinit var smsSender: SmsSubmitCase
    private lateinit var statesList: ArrayList<SmsSendingService.SendingStatus>

    override fun configure(view: GranularSyncContracts.View) {
        this.view = view

        disposable.add(
            getTitle()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.showTitle(it) },
                    { view.closeDialog() }
                )
        )

        disposable.add(
            Single.zip(
                getState(),
                getConflicts(conflictType),
                { state: State, conflicts: MutableList<TrackerImportConflict> ->
                    Pair(state, conflicts)
                }
            ).subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { stateAndConflicts ->
                        view.setState(
                            stateAndConflicts.first,
                            stateAndConflicts.second
                        )
                    },
                    { view.closeDialog() }
                )
        )

        disposable.add(
            getLastSynced().defaultSubscribe(
                schedulerProvider,
                { result -> view.setLastUpdated(result) },
                { error -> Timber.e(error) }
            )
        )
    }

    override fun isSMSEnabled(isTrackerSync: Boolean): Boolean {
        val hasCorrectSmsVersion = if (isTrackerSync) {
            d2.systemInfoModule().versionManager().smsVersion == SMSVersion.V2
        } else {
            true
        }

        val smsModuleIsEnabled =
            d2.smsModule().configCase().smsModuleConfig.blockingGet().isModuleEnabled

        return hasCorrectSmsVersion && smsModuleIsEnabled
    }

    override fun initGranularSync(): LiveData<List<WorkInfo>> {
        var conflictTypeData: SyncStatusDialog.ConflictType? = null
        var dataToDataValues: Data? = null
        when (conflictType) {
            PROGRAM -> conflictTypeData = PROGRAM
            TEI -> conflictTypeData = TEI
            EVENT -> conflictTypeData = EVENT
            DATA_SET -> conflictTypeData = DATA_SET
            DATA_VALUES ->
                dataToDataValues = Data.Builder().putString(UID, recordUid)
                    .putString(CONFLICT_TYPE, DATA_VALUES.name)
                    .putString(ORG_UNIT, dvOrgUnit)
                    .putString(PERIOD_ID, dvPeriodId)
                    .putString(ATTRIBUTE_OPTION_COMBO, dvAttrCombo)
                    .putStringArray(
                        CATEGORY_OPTION_COMBO,
                        getDataSetCatOptCombos().blockingGet().toTypedArray()
                    )
                    .build()
            ALL -> {
            }
        }
        val workName: String
        if (conflictType != ALL) {
            workName = recordUid
            var uid = recordUid
            if (dataToDataValues == null) {
                dataToDataValues = Data.Builder()
                    .putString(UID, recordUid)
                    .putString(CONFLICT_TYPE, conflictTypeData!!.name)
                    .build()
            } else {
                uid = dvOrgUnit + "_" + dvPeriodId + "_" + dvAttrCombo
            }

            val workerItem =
                WorkerItem(
                    uid,
                    WorkerType.GRANULAR,
                    data = dataToDataValues,
                    policy = ExistingWorkPolicy.KEEP
                )

            workManagerController.beginUniqueWork(workerItem)
        } else {
            workName = Constants.INITIAL_SYNC
            workManagerController.syncDataForWorkers(
                Constants.META_NOW, Constants.DATA_NOW, Constants.INITIAL_SYNC
            )
        }
        return workManagerController.getWorkInfosForUniqueWorkLiveData(workName)
    }

    override fun initSMSSync(): LiveData<List<SmsSendingService.SendingStatus>> {
        smsSender = d2.smsModule().smsSubmitCase()
        statesList = ArrayList()
        states = MutableLiveData()

        val convertTask = when (conflictType) {
            EVENT -> smsSender.convertSimpleEvent(recordUid)
            TEI -> {
                // TODO: GET ALL ENROLLMENTS FROM TEI
                val enrollmentUids = UidsHelper.getUidsList(
                    d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(recordUid)
                        .byAggregatedSyncState().`in`(
                            State.TO_POST,
                            State.TO_UPDATE,
                            State.UPLOADING
                        ).blockingGet()
                )
                if (enrollmentUids.isNotEmpty()) {
                    smsSender.convertEnrollment(enrollmentUids[0])
                } else if (!d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(
                    recordUid
                ).blockingIsEmpty()
                ) {
                    smsSender.convertEnrollment(
                        d2.enrollmentModule().enrollments()
                            .byTrackedEntityInstance().eq(recordUid)
                            .one().blockingGet().uid()
                    )
                } else {
                    Single.error(Exception(view.emptyEnrollmentError()))
                }
            }
            DATA_VALUES -> smsSender.convertDataSet(recordUid, dvOrgUnit, dvPeriodId, dvAttrCombo)
            else -> Single.error(Exception(view.unsupportedTask()))
        }

        disposable.add(
            convertTask
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                    { count ->
                        reportState(SmsSendingService.State.CONVERTED, 0, count!!)
                        reportState(
                            SmsSendingService.State.WAITING_COUNT_CONFIRMATION,
                            0,
                            count
                        )
                    },
                    { this.reportError(it) }
                )
        )

        return states
    }

    override fun sendSMS() {
        val startDate = Date()
        disposable.add(
            smsSender.send().doOnNext { state ->
                if (!isLastSendingStateTheSame(state.sent, state.total)) {
                    reportState(
                        if (state.sent == 0) SmsSendingService.State.STARTED
                        else SmsSendingService.State.SENDING,
                        state.sent,
                        state.total
                    )
                }
            }.ignoreElements().doOnComplete {
                reportState(
                    SmsSendingService.State.SENT,
                    0,
                    0
                )
            }.andThen(
                d2.smsModule().configCase().smsModuleConfig
            ).flatMapCompletable { config ->
                if (config.isWaitingForResult) {
                    reportState(SmsSendingService.State.WAITING_RESULT, 0, 0)
                    smsSender.checkConfirmationSms(startDate)
                        .doOnError { throwable ->
                            if (throwable is SmsRepository.ResultResponseException) {
                                val reason = throwable.reason
                                if (reason == SmsRepository.ResultResponseIssue.TIMEOUT) {
                                    reportState(
                                        SmsSendingService.State.WAITING_RESULT_TIMEOUT,
                                        0,
                                        0
                                    )
                                }
                            }
                        }.doOnComplete {
                            reportState(SmsSendingService.State.RESULT_CONFIRMED, 0, 0)
                        }
                } else {
                    Completable.complete()
                }
            }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.io())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onError(e: Throwable) {
                        reportError(e)
                    }

                    override fun onComplete() {
                        reportState(SmsSendingService.State.COMPLETED, 0, 0)
                    }
                })
        )
    }

    private fun isLastSendingStateTheSame(sent: Int, total: Int): Boolean {
        if (statesList.isEmpty()) return false
        val last = statesList[statesList.size - 1]
        return last.state == SmsSendingService.State.SENDING &&
            last.sent == sent &&
            last.total == total
    }

    override fun reportState(state: SmsSendingService.State, sent: Int, total: Int) {
        val submissionId = smsSender.submissionId
        val currentStatus = SmsSendingService.SendingStatus(submissionId, state, null, sent, total)
        statesList.clear()
        statesList.add(currentStatus)
        states.postValue(statesList)
    }

    override fun reportError(throwable: Throwable) {
        Timber.tag(SmsSendingService::class.java.simpleName).e(throwable)
        val submissionId = smsSender.submissionId
        statesList.add(
            SmsSendingService.SendingStatus(
                submissionId,
                SmsSendingService.State.ERROR,
                throwable,
                0,
                0
            )
        )
        states.postValue(statesList)
    }

    @VisibleForTesting
    fun getTitle(): Single<String> {
        return when (conflictType) {
            ALL -> Single.just("Refresh all data")
            PROGRAM -> d2.programModule().programs().uid(recordUid).get().map { it.displayName() }
            TEI ->
                d2.trackedEntityModule().trackedEntityTypes().uid(
                    d2.trackedEntityModule().trackedEntityInstances().uid(recordUid)
                        .blockingGet().trackedEntityType()
                )
                    .get().map { it.displayName() }
            EVENT ->
                d2.programModule().programStages().uid(
                    d2.eventModule().events().uid(recordUid).blockingGet().programStage()
                ).get().map { it.displayName() }
            DATA_SET, DATA_VALUES ->
                d2.dataSetModule().dataSets().withDataSetElements()
                    .uid(recordUid).get()
                    .map { it.displayName() }
        }
    }

    fun getState(): Single<State> {
        return when (conflictType) {
            PROGRAM ->
                d2.programModule().programs().uid(recordUid).get()
                    .map {
                        dhisProgramUtils.getProgramState(it)
                    }
            TEI ->
                d2.trackedEntityModule().trackedEntityInstances().uid(recordUid).get()
                    .map { it.aggregatedSyncState() }
            EVENT ->
                d2.eventModule().events().uid(recordUid).get()
                    .map { it.aggregatedSyncState() }
            DATA_SET ->
                d2.dataSetModule().dataSetInstances()
                    .byDataSetUid().eq(recordUid).get()
                    .map { dataSetInstances ->
                        getStateFromCanditates(dataSetInstances.map { it.state() }.toMutableList())
                    }
            DATA_VALUES ->
                d2.dataSetModule().dataSetInstances()
                    .byOrganisationUnitUid().eq(dvOrgUnit)
                    .byPeriod().eq(dvPeriodId)
                    .byAttributeOptionComboUid().eq(dvAttrCombo)
                    .byDataSetUid().eq(recordUid).get()
                    .map { dataSetInstance ->
                        getStateFromCanditates(dataSetInstance.map { it.state() }.toMutableList())
                    }
            ALL -> Single.just(dhisProgramUtils.getServerState())
        }
    }

    private fun getLastSynced(): Single<SyncDate> {
        return when (conflictType) {
            PROGRAM ->
                d2.programModule().programs().uid(recordUid).get()
                    .map {
                        SyncDate(it.lastUpdated())
                    }
            TEI ->
                d2.trackedEntityModule().trackedEntityInstances().uid(recordUid).get()
                    .map { SyncDate(it.lastUpdated()) }
            EVENT ->
                d2.eventModule().events().uid(recordUid).get()
                    .map { SyncDate(it.lastUpdated()) }
            DATA_SET ->
                d2.dataSetModule().dataSets()
                    .uid(recordUid).get()
                    .map { dataSet ->
                        SyncDate(dataSet.lastUpdated())
                    }
            DATA_VALUES ->
                d2.dataSetModule().dataSetInstances()
                    .byOrganisationUnitUid().eq(dvOrgUnit)
                    .byPeriod().eq(dvPeriodId)
                    .byAttributeOptionComboUid().eq(dvAttrCombo)
                    .byDataSetUid().eq(recordUid).get()
                    .map { dataSetInstance ->
                        dataSetInstance.sortBy { it.lastUpdated() }
                        SyncDate(
                            dataSetInstance.apply {
                                sortBy { it.lastUpdated() }
                            }.first().lastUpdated()
                        )
                    }
            ALL ->
                Single.just(SyncDate(preferenceProvider.lastSync()))
        }
    }

    private fun getConflicts(
        conflictType: SyncStatusDialog.ConflictType
    ): Single<MutableList<TrackerImportConflict>> {
        return when (conflictType) {
            TEI ->
                d2.importModule().trackerImportConflicts()
                    .byTrackedEntityInstanceUid().eq(recordUid).get()
            EVENT ->
                d2.importModule().trackerImportConflicts()
                    .byEventUid().eq(recordUid).get()
            else -> Single.just(mutableListOf())
        }
    }

    fun getStateFromCanditates(stateCandidates: MutableList<State?>): State {
        if (conflictType == DATA_SET) {
            stateCandidates.addAll(
                d2.dataSetModule().dataSetCompleteRegistrations()
                    .byDataSetUid().eq(recordUid)
                    .blockingGet().map { it.syncState() }
            )
        } else {
            stateCandidates.addAll(
                d2.dataSetModule().dataSetCompleteRegistrations()
                    .byOrganisationUnitUid().eq(dvOrgUnit)
                    .byPeriod().eq(dvPeriodId)
                    .byAttributeOptionComboUid().eq(dvAttrCombo)
                    .byDataSetUid().eq(recordUid).get()
                    .blockingGet().map { it.syncState() }
            )
        }

        return when {
            stateCandidates.contains(State.ERROR) -> State.ERROR
            stateCandidates.contains(State.WARNING) -> State.WARNING
            stateCandidates.contains(State.SENT_VIA_SMS) ||
                stateCandidates.contains(State.SYNCED_VIA_SMS) ->
                State.SENT_VIA_SMS
            stateCandidates.contains(State.TO_POST) ||
                stateCandidates.contains(State.UPLOADING) ||
                stateCandidates.contains(State.TO_UPDATE) ->
                State.TO_UPDATE
            else -> State.SYNCED
        }
    }

    private fun getDataSetCatOptCombos(): Single<List<String>> {
        return d2.dataSetModule().dataSets().withDataSetElements().uid(recordUid).get()
            .map {
                it.dataSetElements()?.map { dataSetElement ->
                    if (dataSetElement.categoryCombo() != null) {
                        dataSetElement.categoryCombo()?.uid()
                    } else {
                        d2.dataElementModule()
                            .dataElements()
                            .uid(dataSetElement.dataElement().uid())
                            .blockingGet().categoryComboUid()
                    }
                }?.distinct()
            }
            .flatMap {
                d2.categoryModule().categoryOptionCombos().byCategoryComboUid().`in`(it).get()
            }
            .map { UidsHelper.getUidsList(it) }
    }

    override fun onDettach() {
        disposable.clear()
    }

    override fun displayMessage(message: String?) {
    }

    override fun syncErrors(): List<ErrorViewModel> {
        return arrayListOf<ErrorViewModel>().apply {
            addAll(
                errorMapper.mapD2Error(
                    d2.maintenanceModule().d2Errors().blockingGet()
                )
            )
            addAll(
                errorMapper.mapConflict(
                    d2.importModule().trackerImportConflicts().blockingGet()
                )
            )
            addAll(
                errorMapper.mapFKViolation(
                    d2.maintenanceModule().foreignKeyViolations().blockingGet()
                )
            )
            sortByDescending {
                it.creationDate?.time
            }
        }
    }
}
