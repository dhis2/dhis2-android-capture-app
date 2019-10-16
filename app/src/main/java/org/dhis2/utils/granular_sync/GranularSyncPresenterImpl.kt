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

package org.dhis2.utils.granular_sync

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.schedulers.Schedulers
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.service.SyncGranularWorker
import org.dhis2.usescases.sms.SmsSendingService
import org.dhis2.utils.Constants.*
import org.dhis2.utils.granular_sync.SyncStatusDialog.ConflictType.*
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.common.BaseIdentifiableObject
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase
import org.hisp.dhis.android.core.sms.domain.repository.SmsRepository
import timber.log.Timber
import java.util.*

class GranularSyncPresenterImpl(val d2: D2,
                                val schedulerProvider: SchedulerProvider,
                                private val conflictType: SyncStatusDialog.ConflictType,
                                private val recordUid: String,
                                private val dvOrgUnit: String?,
                                private val dvAttrCombo: String?,
                                private val dvPeriodId: String?,
                                private val workManager: WorkManager) : GranularSyncContracts.Presenter {

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
                                { view.showTitle(it.displayName()!!) },
                                { view.closeDialog() }
                        ))

        disposable.add(
                getState()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                { view.setState(it) },
                                { view.closeDialog() }
                        ))

        if(conflictType==PROGRAM || conflictType == EVENT)
        disposable.add(
                Single.just(conflictType)
                        .flatMap {
                            if (it == PROGRAM)
                                d2.importModule().trackerImportConflicts.byTrackedEntityInstanceUid().eq(recordUid).get()
                            else
                                d2.importModule().trackerImportConflicts.byEventUid().eq(recordUid).get()
                        }
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                { if (it.isNotEmpty()) view.prepareConflictAdapter(it) },
                                { view.closeDialog() }
                        ))
    }

    override fun isSMSEnabled(): Boolean {
        return d2.smsModule().configCase().smsModuleConfig.blockingGet().isModuleEnabled
    }

    override fun initGranularSync(): LiveData<MutableList<WorkInfo>> {
        val syncGranularEventBuilder = OneTimeWorkRequest.Builder(SyncGranularWorker::class.java)
        syncGranularEventBuilder.setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())

        var conflictTypeData: SyncStatusDialog.ConflictType? = null
        var dataToDataValues: Data? = null
        when (conflictType) {
            PROGRAM -> conflictTypeData = PROGRAM
            TEI -> conflictTypeData = TEI
            EVENT -> conflictTypeData = EVENT
            DATA_SET -> conflictTypeData = DATA_SET
            DATA_VALUES -> dataToDataValues = Data.Builder().putString(UID, recordUid)
                    .putString(CONFLICT_TYPE, DATA_VALUES.name)
                    .putString(ORG_UNIT, dvOrgUnit)
                    .putString(PERIOD_ID, dvPeriodId)
                    .putString(ATTRIBUTE_OPTION_COMBO, dvAttrCombo)
                    .putStringArray(CATEGORY_OPTION_COMBO, getDataSetCatOptCombos().blockingGet().toTypedArray())
                    .build()
        }
        var uid = recordUid
        if (dataToDataValues == null) {
            syncGranularEventBuilder.setInputData(Data.Builder().putString(UID, recordUid).putString(CONFLICT_TYPE, conflictTypeData!!.name).build())
        } else {
            syncGranularEventBuilder.setInputData(dataToDataValues)
            uid = dvOrgUnit + "_" + dvPeriodId + "_" + dvAttrCombo
        }
        val request = syncGranularEventBuilder.build()
        workManager.beginUniqueWork(uid, ExistingWorkPolicy.KEEP, request).enqueue()

        return workManager.getWorkInfosForUniqueWorkLiveData(uid)
    }

    override fun initSMSSync(): LiveData<List<SmsSendingService.SendingStatus>> {
        smsSender = d2.smsModule().smsSubmitCase()
        statesList = ArrayList()
        states = MutableLiveData()

        val convertTask = when (conflictType) {
            EVENT -> smsSender.convertSimpleEvent(recordUid)
            TEI -> {
                //TODO: GET ALL ENROLLMENTS FROM TEI
                val enrollmentUids = UidsHelper.getUidsList(d2.enrollmentModule().enrollments.byTrackedEntityInstance().eq(recordUid)
                        .byState().`in`(State.TO_POST, State.TO_UPDATE).blockingGet())
                smsSender.convertEnrollment(enrollmentUids[0])
            }
            DATA_VALUES -> smsSender.convertDataSet(recordUid, dvOrgUnit, dvPeriodId, dvAttrCombo)
            else -> Single.error(Exception("This convertTask is not supported"))
        }

        disposable.add(
                convertTask
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                                { count ->
                                    reportState(SmsSendingService.State.CONVERTED, 0, count!!)
                                    reportState(SmsSendingService.State.WAITING_COUNT_CONFIRMATION, 0, count)
                                },
                                { this.reportError(it) }
                        )
        )

        return states
    }

    override fun sendSMS() {
        val startDate = Date()
        disposable.add(smsSender.send().doOnNext { state ->
            if (!isLastSendingStateTheSame(state.sent, state.total)) {
                reportState(SmsSendingService.State.SENDING, state.sent, state.total)
            }
        }.ignoreElements().doOnComplete { reportState(SmsSendingService.State.SENT, 0, 0) }.andThen(
                d2.smsModule().configCase().smsModuleConfig
        ).flatMapCompletable { config ->
            if (config.isWaitingForResult) {
                reportState(SmsSendingService.State.WAITING_RESULT, 0, 0)
                smsSender.checkConfirmationSms(startDate)
                        .doOnError { throwable ->
                            if (throwable is SmsRepository.ResultResponseException) {
                                val reason = throwable.reason
                                if (reason == SmsRepository.ResultResponseIssue.TIMEOUT) {
                                    reportState(SmsSendingService.State.WAITING_RESULT_TIMEOUT, 0, 0)
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
                }))
    }

    private fun isLastSendingStateTheSame(sent: Int, total: Int): Boolean {
        if (statesList.isEmpty()) return false
        val last = statesList[statesList.size - 1]
        return last.state == SmsSendingService.State.SENDING && last.sent == sent && last.total == total
    }

    override fun reportState(state: SmsSendingService.State, sent: Int, total: Int) {
        val submissionId = smsSender.submissionId
        val currentStatus = SmsSendingService.SendingStatus(submissionId, state, null, sent, total)
        statesList.add(currentStatus)
        states.postValue(statesList)
    }

    override fun reportError(throwable: Throwable) {
        Timber.tag(SmsSendingService::class.java.simpleName).e(throwable)
        val submissionId = smsSender.submissionId
        statesList.add(SmsSendingService.SendingStatus(submissionId, SmsSendingService.State.ERROR, throwable, 0, 0))
        states.postValue(statesList)
    }

    @VisibleForTesting
    fun getTitle(): Single<out BaseIdentifiableObject> {
        return when (conflictType) {
            PROGRAM -> d2.programModule().programs.uid(recordUid).get()
            TEI ->
                d2.trackedEntityModule().trackedEntityTypes.uid(
                        d2.trackedEntityModule().trackedEntityInstances.uid(recordUid)
                                .blockingGet().trackedEntityType())
                        .get()
            EVENT ->
                d2.programModule().programStages.uid(
                        d2.eventModule().events.uid(recordUid).blockingGet().programStage()).get()
            DATA_SET -> d2.dataSetModule().dataSets().withDataSetElements().uid(recordUid).get()
            DATA_VALUES -> d2.dataSetModule().dataSets().withDataSetElements().uid(recordUid).get()
        }
    }

    private fun getState(): Single<State> {
        return when (conflictType) {
            PROGRAM -> d2.programModule().programs.uid(recordUid).get()
                    .map {
                        if (it.programType() == ProgramType.WITHOUT_REGISTRATION) {
                            val eventRepository = d2.eventModule().events.byProgramUid().eq(it.uid())
                            if (eventRepository.byState().`in`(State.ERROR).blockingGet().isNotEmpty())
                                State.ERROR
                            else if (eventRepository.byState().`in`(State.WARNING).blockingGet().isNotEmpty())
                                State.WARNING
                            else if (eventRepository.byState().`in`(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).blockingGet().isNotEmpty())
                                State.SENT_VIA_SMS
                            else if (eventRepository.byState().`in`(State.TO_UPDATE, State.TO_POST).blockingGet().isNotEmpty() || eventRepository.byDeleted().isTrue.blockingGet().isNotEmpty())
                                State.TO_UPDATE
                            else
                                State.SYNCED
                        } else {
                            val teiRepository = d2.trackedEntityModule().trackedEntityInstances.byProgramUids(Collections.singletonList(it.uid()))
                            if (teiRepository.byState().`in`(State.ERROR).blockingGet().isNotEmpty())
                                State.ERROR
                            else if (teiRepository.byState().`in`(State.WARNING).blockingGet().isNotEmpty())
                                State.WARNING
                            else if (teiRepository.byState().`in`(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).blockingGet().isNotEmpty())
                                State.SENT_VIA_SMS
                            else if (teiRepository.byState().`in`(State.TO_UPDATE, State.TO_POST).blockingGet().isNotEmpty() || teiRepository.byDeleted().isTrue.blockingGet().isNotEmpty())
                                State.TO_UPDATE
                            else
                                State.SYNCED
                        }
                    }
            TEI -> d2.trackedEntityModule().trackedEntityInstances.uid(recordUid).get()
                    .map { it.state() }
            EVENT -> d2.eventModule().events.uid(recordUid).get()
                    .map { it.state() }
            DATA_SET -> d2.dataSetModule().dataSets().withDataSetElements().uid(recordUid).get()
                    .map { it.dataSetElements() }
                    .map {
                        var state = State.SYNCED
                        it.forEach { de ->
                            d2.dataValueModule().dataValues().byDataElementUid().eq(de.dataElement().uid()).blockingGet().forEach { dv ->
                                if (dv.state() != State.SYNCED)
                                    state = State.TO_UPDATE
                            }
                        }
                        state
                    }
            DATA_VALUES -> getDataSetCatOptCombos()
                    .flatMap {
                        d2.dataValueModule().dataValues()
                                .byOrganisationUnitUid().eq(dvOrgUnit)
                                .byAttributeOptionComboUid().eq(dvAttrCombo)
                                .byPeriod().eq(dvPeriodId).byCategoryOptionComboUid().`in`(it).get()
                    }
                    .map {
                        var state = State.SYNCED
                        it.forEach { dv ->
                            if (dv.state() != State.SYNCED)
                                state = State.TO_UPDATE
                        }
                        state
                    }
        }
    }

    private fun getDataSetCatOptCombos(): Single<List<String>> {
        return d2.dataSetModule().dataSets().withDataSetElements().uid(recordUid).get()
                .map {
                    it.dataSetElements()?.map { dataSetElement ->
                        if (dataSetElement.categoryCombo() != null)
                            dataSetElement.categoryCombo()?.uid()
                        else
                            d2.dataElementModule().dataElements().uid(dataSetElement.dataElement().uid()).blockingGet().categoryComboUid()
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

}

