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

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.Constants.ATTRIBUTE_OPTION_COMBO
import org.dhis2.commons.Constants.CATEGORY_OPTION_COMBO
import org.dhis2.commons.Constants.CONFLICT_TYPE
import org.dhis2.commons.Constants.ORG_UNIT
import org.dhis2.commons.Constants.PERIOD_ID
import org.dhis2.commons.Constants.UID
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.dhis2.commons.sync.ConflictType
import org.dhis2.commons.sync.ConflictType.ALL
import org.dhis2.commons.sync.ConflictType.DATA_SET
import org.dhis2.commons.sync.ConflictType.DATA_VALUES
import org.dhis2.commons.sync.ConflictType.EVENT
import org.dhis2.commons.sync.ConflictType.PROGRAM
import org.dhis2.commons.sync.ConflictType.TEI
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import timber.log.Timber

class GranularSyncPresenter(
    val d2: D2,
    private val view: GranularSyncContracts.View,
    private val repository: GranularSyncRepository,
    private val dhisProgramUtils: DhisProgramUtils,
    val schedulerProvider: SchedulerProvider,
    private val conflictType: ConflictType,
    private val recordUid: String,
    private val dvOrgUnit: String?,
    private val dvAttrCombo: String?,
    private val dvPeriodId: String?,
    private val workManagerController: WorkManagerController,
    private val errorMapper: ErrorModelMapper,
    private val preferenceProvider: PreferenceProvider,
    private val smsSyncProvider: SMSSyncProvider,
    private val resourceManager: ResourceManager
) {

    private var disposable: CompositeDisposable = CompositeDisposable()
    private lateinit var states: MutableLiveData<List<SmsSendingService.SendingStatus>>
    private lateinit var statesList: ArrayList<SmsSendingService.SendingStatus>

    private val _currentState = MutableStateFlow<SyncUiState?>(null)
    val currentState: StateFlow<SyncUiState?> = _currentState

    init {
        disposable.add(
            Single.zip(
                getState(),
                getLastSynced()
            ) { state, lastSync ->
                SyncUiState(
                    syncState = state,
                    title = getTitleForState(state),
                    lastSyncDate = lastSync,
                    message = getMessageForState(state),
                    mainActionLabel = getMainActionLabel(state),
                    secondaryActionLabel = getSecondaryActionLabel(state),
                    content = getContent(state)
                )
            }.defaultSubscribe(
                schedulerProvider,
                { newState ->
                    _currentState.update { newState }
                },
                {
                    Timber.e(it)
                }
            )
        )
    }

    private fun getTitleForState(state: State): String {
        return when (state) {
            State.TO_POST,
            State.TO_UPDATE -> resourceManager.getString(R.string.sync_dialog_title_not_synced)
            State.ERROR -> resourceManager.getString(R.string.sync_dialog_title_error)
            State.RELATIONSHIP,
            State.SYNCED -> resourceManager.getString(R.string.sync_dialog_title_synced)
            State.WARNING -> resourceManager.getString(R.string.sync_dialog_title_warning)
            State.UPLOADING -> resourceManager.getString(R.string.sync_dialog_title_syncing)
            State.SENT_VIA_SMS,
            State.SYNCED_VIA_SMS -> resourceManager.getString(R.string.sync_dialog_title_sms_syced)
        }
    }

    private fun getMessageForState(state: State): String? {
        return when (state) {
            State.TO_POST,
            State.TO_UPDATE -> getNotSyncedMessage()
            State.SYNCED -> getSyncedMessage()
            State.SENT_VIA_SMS,
            State.SYNCED_VIA_SMS -> getSmsSyncedMessage()
            State.ERROR,
            State.WARNING,
            State.RELATIONSHIP,
            State.UPLOADING -> null
        }
    }

    private fun getNotSyncedMessage(): String? {
        return when (conflictType) {
            ALL -> resourceManager.getString(R.string.sync_dialog_message_not_synced_all)
            DATA_SET,
            DATA_VALUES,
            PROGRAM -> resourceManager.getString(R.string.sync_dialog_message_not_synced_program, getMessageArgument())
            TEI, EVENT -> resourceManager.getString(R.string.sync_dialog_message_not_synced_tei, getMessageArgument())
        }
    }

    private fun getSyncedMessage(): String? {
        return when (conflictType) {
            ALL -> resourceManager.getString(R.string.sync_dialog_message_synced_all)
            DATA_SET,
            DATA_VALUES,
            PROGRAM -> resourceManager.getString(R.string.sync_dialog_message_synced_program, getMessageArgument())
            TEI, EVENT -> resourceManager.getString(R.string.sync_dialog_message_synced_tei, getMessageArgument())
        }
    }

    private fun getSmsSyncedMessage(): String? {
        return when (conflictType) {
            ALL,
            DATA_SET,
            PROGRAM -> null
            DATA_VALUES,
            TEI,
            EVENT -> resourceManager.getString(R.string.sync_dialog_message_sms_synced)
        }
    }

    private fun getMessageArgument(): String {
        return getTitle().blockingGet()
    }

    private fun getMainActionLabel(state: State): String? {
        return when (state) {
            State.TO_POST,
            State.SENT_VIA_SMS,
            State.SYNCED_VIA_SMS,
            State.TO_UPDATE -> resourceManager.getString(R.string.sync_dialog_action_send)
            State.ERROR,
            State.SYNCED,
            State.WARNING -> resourceManager.getString(R.string.sync_dialog_action_refresh)
            State.UPLOADING,
            State.RELATIONSHIP -> null
        }
    }

    private fun getSecondaryActionLabel(state: State): String? {
        return when (state) {
            State.UPLOADING,
            State.RELATIONSHIP -> null
            State.TO_POST,
            State.TO_UPDATE,
            State.ERROR,
            State.SYNCED,
            State.SENT_VIA_SMS,
            State.SYNCED_VIA_SMS,
            State.WARNING -> resourceManager.getString(R.string.sync_dialog_action_not_now)
        }
    }

    private fun getContent(state: State): List<SyncStatusItem> {
        return when (state) {
            State.TO_POST,
            State.TO_UPDATE -> getContentItems(State.TO_POST, State.TO_UPDATE)
            State.ERROR -> getContentItems(State.ERROR, State.WARNING, State.TO_UPDATE, State.TO_POST)
            State.WARNING -> getContentItems(State.WARNING, State.TO_POST, State.TO_UPDATE)
            State.UPLOADING,
            State.RELATIONSHIP,
            State.SYNCED,
            State.SENT_VIA_SMS,
            State.SYNCED_VIA_SMS -> emptyList()
        }
    }

    private fun getContentItems(vararg states: State): List<SyncStatusItem> {
        return when (conflictType) {
            ALL -> repository.getHomeItemsWithStates(*states)
            PROGRAM -> TODO()/*repository.getProgramItemsWithStates(states)*/
            TEI -> TODO()/*repository.getTeiItemsWithStates(states)*/
            EVENT -> TODO()/*repository.getEventItemsWithStates(states)*/
            DATA_SET -> TODO()
            DATA_VALUES -> TODO()
        }
    }

    fun configure() {
        disposable.add(
            Single.zip(
                getState(),
                getConflicts(conflictType)
            ) { state: State, conflicts: MutableList<TrackerImportConflict> ->
                Pair(state, conflicts)
            }.subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { stateAndConflicts ->
                        view.setState(
                            stateAndConflicts.first,
                            stateAndConflicts.second
                        )
                    },
                    { error ->
                        Timber.e(error)
                        view.closeDialog()
                    }
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

    fun isSMSEnabled(showSms: Boolean): Boolean {
        return smsSyncProvider.isSMSEnabled(conflictType == TEI) && showSms
    }

    fun canSendSMS(): Boolean {
        return when (conflictType) {
            ALL,
            PROGRAM,
            DATA_SET -> false
            TEI,
            EVENT,
            DATA_VALUES -> true
        }
    }

    fun initGranularSync(): LiveData<List<WorkInfo>> {
        var conflictTypeData: ConflictType? = null
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
        var workName: String
        if (conflictType != ALL) {
            workName = recordUid
            if (dataToDataValues == null) {
                dataToDataValues = Data.Builder()
                    .putString(UID, recordUid)
                    .putString(CONFLICT_TYPE, conflictTypeData!!.name)
                    .build()
            } else {
                workName = dvOrgUnit + "_" + dvPeriodId + "_" + dvAttrCombo
            }

            val workerItem =
                WorkerItem(
                    workName,
                    WorkerType.GRANULAR,
                    data = dataToDataValues,
                    policy = ExistingWorkPolicy.KEEP
                )

            workManagerController.beginUniqueWork(workerItem)
        } else {
            workName = Constants.INITIAL_SYNC
            workManagerController.syncDataForWorker(Constants.DATA_NOW, Constants.INITIAL_SYNC)
        }
        return workManagerController.getWorkInfosForUniqueWorkLiveData(workName)
    }

    // NO PLAY SERVICES
    fun initSMSSync(): LiveData<List<SmsSendingService.SendingStatus>> {
        statesList = ArrayList()
        states = MutableLiveData()

        disposable.add(
            smsSyncProvider.getConvertTask()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.io())
                .subscribe(
                    { countResult ->
                        if (countResult is ConvertTaskResult.Count) {
                            updateStateList(
                                SmsSendingService.SendingStatus(
                                    smsSyncProvider.smsSender.submissionId,
                                    SmsSendingService.State.CONVERTED,
                                    null,
                                    0,
                                    countResult.smsCount
                                )
                            )
                            updateStateList(
                                SmsSendingService.SendingStatus(
                                    smsSyncProvider.smsSender.submissionId,
                                    SmsSendingService.State.WAITING_COUNT_CONFIRMATION,
                                    null,
                                    0,
                                    countResult.smsCount
                                )
                            )
                        }
                    },
                    {
                        updateStateList(
                            SmsSendingService.SendingStatus(
                                smsSyncProvider.smsSender.submissionId,
                                SmsSendingService.State.ERROR,
                                it,
                                0,
                                0
                            )
                        )
                    }
                )
        )

        return states
    }

    fun restartSmsSender() {
        smsSyncProvider.restartSmsSender()
    }

    // PLAY SERVICES
    private fun initSMSSyncPlayServices() {
        disposable.add(
            smsSyncProvider.getConvertTask()
                .filter {
                    it is ConvertTaskResult.Message
                }
                .map { result ->
                    (result as ConvertTaskResult.Message).smsMessage
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { message ->
                        view.openSmsApp(message, smsSyncProvider.getGatewayNumber())
                    },
                    { error -> Timber.e(error) }
                )
        )
    }

    fun sendSMS() {
        disposable.add(
            smsSyncProvider.sendSms(
                doOnNext = { sendingStatus: SmsSendingService.SendingStatus ->
                    if (!isLastSendingStateTheSame(sendingStatus.sent, sendingStatus.total)) {
                        updateStateList(sendingStatus)
                    }
                },
                doOnNewState = {
                    updateStateList(it)
                }
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.io())
                .subscribe(
                    {
                        updateStateList(
                            SmsSendingService.SendingStatus(
                                smsSyncProvider.smsSender.submissionId,
                                SmsSendingService.State.COMPLETED,
                                null,
                                0,
                                0
                            )
                        )
                    },
                    {
                        updateStateList(
                            SmsSendingService.SendingStatus(
                                smsSyncProvider.smsSender.submissionId,
                                SmsSendingService.State.ERROR,
                                it,
                                0,
                                0
                            )
                        )
                    }
                )
        )
    }

    fun onSmsNotAccepted() {
        updateStateList(
            smsSyncProvider.onSmsNotAccepted()
        )
    }

    private fun updateStatusToSentBySMS() {
        smsSyncProvider.smsSender.markAsSentViaSMS().blockingAwait()
        view.updateState(State.SENT_VIA_SMS)
    }

    private fun updateStatusToSyncedWithSMS() {
        smsSyncProvider.smsSender.markAsSentViaSMS().blockingAwait()
        view.updateState(State.SYNCED_VIA_SMS)
    }

    private fun isLastSendingStateTheSame(sent: Int, total: Int): Boolean {
        if (statesList.isEmpty()) return false
        val last = statesList[statesList.size - 1]
        return last.state == SmsSendingService.State.SENDING &&
            last.sent == sent &&
            last.total == total
    }

    private fun updateStateList(currentStatus: SmsSendingService.SendingStatus) {
        if (currentStatus.state != SmsSendingService.State.ERROR) {
            statesList.clear()
        }
        statesList.add(currentStatus)
        states.postValue(statesList)
    }

    fun onSmsNotManuallySent(context: Context) {
        view.logSmsNotSent()
        smsSyncProvider.unregisterSMSReceiver(context)
        restartSmsSender()
    }

    fun onSmsSyncClick(
        callback: (LiveData<List<SmsSendingService.SendingStatus>>) -> Unit
    ) {
        if (smsSyncProvider.isPlayServicesEnabled()) {
            view.logOpeningSmsApp()
            initSMSSyncPlayServices()
        } else if (view.checkSmsPermission()) {
            callback(initSMSSync())
        }
    }

    fun onSmsManuallySent(
        context: Context,
        confirmationCallback: (LiveData<Boolean?>) -> Unit
    ) {
        if (smsSyncProvider.expectsResponseSMS()) {
            view.logWaitingForServerResponse()
            smsSyncProvider.waitForSMSResponse(
                context,
                smsSyncProvider.getGatewayNumber(),
                onSuccess = {
                    confirmationCallback(smsSyncProvider.observeConfirmationNumber())
                },
                onFailure = {
                    view.logSmsReachedServerError()
                    updateStatusToSentBySMS()
                    restartSmsSender()
                }
            )
        } else {
            view.logSmsSent()
            updateStatusToSentBySMS()
            restartSmsSender()
        }
    }

    fun onConfirmationMessageStateChanged(messageReceived: Boolean?) {
        messageReceived?.let {
            when (it) {
                true -> {
                    view.logSmsReachedServer()
                    updateStatusToSyncedWithSMS()
                }
                false -> {
                    view.logSmsReachedServerError()
                    updateStatusToSentBySMS()
                }
            }
        }
        restartSmsSender()
    }

    @VisibleForTesting
    fun getTitle(): Single<String> {
        return when (conflictType) {
            ALL -> Single.just("")
            PROGRAM -> d2.programModule().programs().uid(recordUid).get().map { it.displayName() }
            TEI -> {
                val enrollment =
                    d2.enrollmentModule().enrollments().uid(recordUid).blockingGet()
                d2.trackedEntityModule().trackedEntityTypes().uid(
                    d2.trackedEntityModule().trackedEntityInstances()
                        .uid(enrollment.trackedEntityInstance()!!)
                        .blockingGet().trackedEntityType()
                )
                    .get().map { it.displayName() }
            }
            EVENT ->
                d2.programModule().programStages().uid(
                    d2.eventModule().events().uid(recordUid).blockingGet().programStage()
                ).get().map { it.displayName() }
            DATA_SET, DATA_VALUES ->
                d2.dataSetModule().dataSets().uid(recordUid).get()
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
            TEI -> {
                val enrollment = d2.enrollmentModule().enrollments().uid(recordUid).blockingGet()
                d2.trackedEntityModule().trackedEntityInstances()
                    .uid(enrollment.trackedEntityInstance()).get()
                    .map { it.aggregatedSyncState() }
            }
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
            TEI -> {
                val enrollment = d2.enrollmentModule().enrollments().uid(recordUid).blockingGet()
                d2.trackedEntityModule().trackedEntityInstances()
                    .uid(enrollment.trackedEntityInstance()).get()
                    .map { SyncDate(it.lastUpdated()) }
            }
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
        conflictType: ConflictType
    ): Single<MutableList<TrackerImportConflict>> {
        return when (conflictType) {
            TEI -> {
                val enrollment = d2.enrollmentModule().enrollments().uid(recordUid).blockingGet()
                d2.importModule().trackerImportConflicts()
                    .byTrackedEntityInstanceUid().eq(enrollment.trackedEntityInstance()).get()
            }
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

    fun onDettach() {
        disposable.clear()
    }

    fun displayMessage(message: String?) {
    }

    fun syncErrors(): List<ErrorViewModel> {
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

    fun trackedEntityTypeNameFromEnrollment(enrollmentUid: String): String? {
        return d2.enrollmentModule().enrollments()
            .uid(enrollmentUid)
            .get().flatMap { enrollment ->
                d2.trackedEntityModule().trackedEntityInstances()
                    .uid(enrollment.trackedEntityInstance())
                    .get()
            }.flatMap { tei ->
                d2.trackedEntityModule().trackedEntityTypes()
                    .uid(tei.trackedEntityType())
                    .get()
            }.blockingGet().displayName()
    }
}
