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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.Constants
import org.dhis2.commons.Constants.ATTRIBUTE_OPTION_COMBO
import org.dhis2.commons.Constants.CATEGORY_OPTION_COMBO
import org.dhis2.commons.Constants.CONFLICT_TYPE
import org.dhis2.commons.Constants.ORG_UNIT
import org.dhis2.commons.Constants.PERIOD_ID
import org.dhis2.commons.Constants.UID
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.sync.ConflictType
import org.dhis2.commons.sync.ConflictType.ALL
import org.dhis2.commons.sync.ConflictType.DATA_SET
import org.dhis2.commons.sync.ConflictType.DATA_VALUES
import org.dhis2.commons.sync.ConflictType.EVENT
import org.dhis2.commons.sync.ConflictType.PROGRAM
import org.dhis2.commons.sync.ConflictType.TEI
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.common.State
import timber.log.Timber

class GranularSyncPresenter(
    val d2: D2,
    private val view: GranularSyncContracts.View,
    private val repository: GranularSyncRepository,
    val schedulerProvider: SchedulerProvider,
    private val dispatcher: DispatcherProvider,
    private val syncContext: SyncContext,
    private val workManagerController: WorkManagerController,
    private val smsSyncProvider: SMSSyncProvider,
) : ViewModel() {

    private var disposable: CompositeDisposable = CompositeDisposable()
    private lateinit var states: MutableLiveData<List<SmsSendingService.SendingStatus>>
    private lateinit var statesList: ArrayList<SmsSendingService.SendingStatus>
    private var refreshing = false
    private val _currentState = MutableStateFlow<SyncUiState?>(null)
    val currentState: StateFlow<SyncUiState?> = _currentState

    private fun loadSyncInfo(forcedState: State? = null) {
        viewModelScope.launch(dispatcher.io()) {
            val syncState = async {
                repository.getUiState(forcedState)
            }.await()
            val dismissOnUpdate = refreshing && syncState.syncState == State.SYNCED
            refreshing = false
            _currentState.update {
                syncState.copy(shouldDismissOnUpdate = dismissOnUpdate)
            }
        }
    }

    fun refreshContent() {
        refreshing = _currentState.value != null
        loadSyncInfo()
    }

    fun isSMSEnabled(showSms: Boolean): Boolean {
        return smsSyncProvider.isSMSEnabled(syncContext.conflictType() == TEI) && showSms
    }

    fun canSendSMS(): Boolean {
        return when (syncContext.conflictType()) {
            ALL,
            PROGRAM,
            DATA_SET,
            -> false
            TEI,
            EVENT,
            DATA_VALUES,
            -> true
        }
    }

    fun initGranularSync(): LiveData<List<WorkInfo>> {
        var conflictTypeData: ConflictType? = null
        var dataToDataValues: Data? = null
        when (syncContext.conflictType()) {
            PROGRAM -> conflictTypeData = PROGRAM
            TEI -> conflictTypeData = TEI
            EVENT -> conflictTypeData = EVENT
            DATA_SET -> conflictTypeData = DATA_SET
            DATA_VALUES ->
                with(syncContext as SyncContext.DataSetInstance) {
                    dataToDataValues = Data.Builder().putString(UID, recordUid())
                        .putString(CONFLICT_TYPE, DATA_VALUES.name)
                        .putString(ORG_UNIT, orgUnitUid)
                        .putString(PERIOD_ID, periodId)
                        .putString(ATTRIBUTE_OPTION_COMBO, attributeOptionComboUid)
                        .putStringArray(
                            CATEGORY_OPTION_COMBO,
                            getDataSetCatOptCombos().blockingGet().toTypedArray(),
                        )
                        .build()
                }
            ALL -> { // Do nothing
            }
        }
        var workName: String
        if (syncContext.conflictType() != ALL) {
            workName = syncContext.recordUid()
            if (dataToDataValues == null) {
                dataToDataValues = Data.Builder()
                    .putString(UID, syncContext.recordUid())
                    .putString(CONFLICT_TYPE, conflictTypeData!!.name)
                    .build()
            } else {
                workName = with(syncContext as SyncContext.DataSetInstance) {
                    orgUnitUid + "_" + periodId + "_" + attributeOptionComboUid
                }
            }

            val workerItem =
                WorkerItem(
                    workName,
                    WorkerType.GRANULAR,
                    data = dataToDataValues,
                    policy = ExistingWorkPolicy.KEEP,
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
                                    countResult.smsCount,
                                ),
                            )
                            updateStateList(
                                SmsSendingService.SendingStatus(
                                    smsSyncProvider.smsSender.submissionId,
                                    SmsSendingService.State.WAITING_COUNT_CONFIRMATION,
                                    null,
                                    0,
                                    countResult.smsCount,
                                ),
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
                                0,
                            ),
                        )
                    },
                ),
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
                    { error -> Timber.e(error) },
                ),
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
                },
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
                                0,
                            ),
                        )
                    },
                    {
                        updateStateList(
                            SmsSendingService.SendingStatus(
                                smsSyncProvider.smsSender.submissionId,
                                SmsSendingService.State.ERROR,
                                it,
                                0,
                                0,
                            ),
                        )
                    },
                ),
        )
    }

    fun onSmsNotAccepted() {
        updateStateList(
            smsSyncProvider.onSmsNotAccepted(),
        )
    }

    private fun updateStatusToSentBySMS() {
        smsSyncProvider.smsSender.markAsSentViaSMS().blockingAwait()
    }

    private fun updateStatusToSyncedWithSMS() {
        smsSyncProvider.smsSender.markAsSentViaSMS().blockingAwait()
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
        smsSyncProvider.unregisterSMSReceiver(context)
        restartSmsSender()
    }

    fun onSmsSyncClick(callback: (LiveData<List<SmsSendingService.SendingStatus>>) -> Unit) {
        if (smsSyncProvider.isPlayServicesEnabled()) {
            initSMSSyncPlayServices()
        } else if (view.checkSmsPermission()) {
            callback(initSMSSync())
        }
    }

    fun onSmsManuallySent(context: Context, confirmationCallback: (LiveData<Boolean?>) -> Unit) {
        if (smsSyncProvider.expectsResponseSMS()) {
            smsSyncProvider.waitForSMSResponse(
                context,
                smsSyncProvider.getGatewayNumber(),
                onSuccess = {
                    confirmationCallback(smsSyncProvider.observeConfirmationNumber())
                },
                onFailure = {
                    updateStatusToSentBySMS()
                    restartSmsSender()
                    loadSyncInfo()
                },
            )
        } else {
            updateStatusToSentBySMS()
            restartSmsSender()
            loadSyncInfo()
        }
    }

    fun onConfirmationMessageStateChanged(messageReceived: Boolean?) {
        messageReceived?.let {
            when (it) {
                true -> {
                    updateStatusToSyncedWithSMS()
                }
                false -> {
                    updateStatusToSentBySMS()
                }
            }
            loadSyncInfo()
        }
        restartSmsSender()
    }

    private fun getDataSetCatOptCombos(): Single<List<String>> {
        return d2.dataSetModule().dataSets().withDataSetElements().uid(syncContext.recordUid())
            .get()
            .map {
                it.dataSetElements()?.mapNotNull { dataSetElement ->
                    if (dataSetElement.categoryCombo() != null) {
                        dataSetElement.categoryCombo()?.uid()
                    } else {
                        d2.dataElementModule()
                            .dataElements()
                            .uid(dataSetElement.dataElement().uid())
                            .blockingGet()?.categoryComboUid()
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

    fun manageWorkInfo(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.BLOCKED,
            WorkInfo.State.RUNNING,
            -> {
                loadSyncInfo(State.UPLOADING)
            }
            WorkInfo.State.SUCCEEDED,
            WorkInfo.State.FAILED,
            WorkInfo.State.CANCELLED,
            -> {
                loadSyncInfo()
            }
        }
    }
}
