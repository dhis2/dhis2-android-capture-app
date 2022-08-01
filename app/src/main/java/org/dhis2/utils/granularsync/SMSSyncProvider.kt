package org.dhis2.utils.granularsync

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableSingleObserver
import java.util.Date
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase
import org.hisp.dhis.android.core.sms.domain.repository.SmsRepository
import org.hisp.dhis.android.core.systeminfo.SMSVersion
import timber.log.Timber

class SMSSyncProvider(
    private val d2: D2,
    private val conflictType: SyncStatusDialog.ConflictType,
    private val recordUid: String,
    private val dvOrgUnit: String?,
    private val dvAttrCombo: String?,
    private val dvPeriodId: String?,
    private val resourceManager: ResourceManager
) {
    private val smsSender: SmsSubmitCase by lazy {
        d2.smsModule().smsSubmitCase()
    }

    fun isSMSEnabled(isTrackerSync: Boolean): Boolean {
        val hasCorrectSmsVersion = if (isTrackerSync) {
            d2.systemInfoModule().versionManager().smsVersion == SMSVersion.V2
        } else {
            true
        }

        val smsModuleIsEnabled =
            d2.smsModule().configCase().smsModuleConfig.blockingGet().isModuleEnabled

        return hasCorrectSmsVersion && smsModuleIsEnabled
    }

    fun getConvertTask(): Single<Int> {
        return when (conflictType) {
            SyncStatusDialog.ConflictType.EVENT -> {
                if (d2.eventModule().events().uid(recordUid).blockingGet().enrollment() == null) {
                    smsSender.convertSimpleEvent(recordUid)
                } else {
                    smsSender.convertTrackerEvent(recordUid)
                }
            }
            SyncStatusDialog.ConflictType.TEI -> {
                if (d2.enrollmentModule().enrollments().uid(recordUid).blockingExists()) {
                    smsSender.convertEnrollment(recordUid)
                } else {
                    Single.error(
                        Exception(
                            resourceManager.getString(R.string.granular_sync_enrollments_empty)
                        )
                    )
                }
            }
            SyncStatusDialog.ConflictType.DATA_VALUES -> smsSender.convertDataSet(
                recordUid,
                dvOrgUnit,
                dvPeriodId,
                dvAttrCombo
            )
            else -> Single.error(
                Exception(
                    resourceManager.getString(R.string.granular_sync_unsupported_task)
                )
            )
        }
    }

    fun sendSms(
        doOnNext: (sendingStatus: SmsSendingService.SendingStatus) -> Unit,
        doOnNewState: (sendingStatus: SmsSendingService.SendingStatus) -> Unit
    ): Completable {
        val startDate = Date()

        return smsSender.send().doOnNext { state ->

            doOnNext(
                reportState(
                    if (state.sent == 0) {
                        SmsSendingService.State.STARTED
                    } else {
                        SmsSendingService.State.SENDING
                    },
                    state.sent,
                    state.total
                )
            )
        }
            .ignoreElements()
            .doOnComplete {
                doOnNewState(
                    reportState(SmsSendingService.State.SENT, 0, 0)
                )
            }
            .andThen(d2.smsModule().configCase().smsModuleConfig)
            .flatMapCompletable { config ->
                if (config.isWaitingForResult) {
                    doOnNewState(
                        reportState(SmsSendingService.State.WAITING_RESULT, 0, 0)
                    )
                    smsSender.checkConfirmationSms(startDate)
                        .doOnError { throwable ->
                            if (throwable is SmsRepository.ResultResponseException) {
                                val reason = throwable.reason
                                if (reason == SmsRepository.ResultResponseIssue.TIMEOUT) {
                                    doOnNewState(
                                        reportState(
                                            SmsSendingService.State.WAITING_RESULT_TIMEOUT,
                                            0,
                                            0
                                        )
                                    )
                                }
                            }
                        }.doOnComplete {
                            doOnNewState(
                                reportState(SmsSendingService.State.RESULT_CONFIRMED, 0, 0)
                            )
                        }
                } else {
                    Completable.complete()
                }
            }
    }

    fun onConvertingObserver(onComplete: (SmsSendingService.SendingStatus) -> Unit) =
        object : DisposableSingleObserver<Int>() {
            override fun onSuccess(count: Int) {
                onComplete(
                    reportState(SmsSendingService.State.CONVERTED, 0, count)
                )
                onComplete(
                    reportState(SmsSendingService.State.WAITING_COUNT_CONFIRMATION, 0, count)
                )
            }

            override fun onError(e: Throwable) {
                onComplete(
                    reportError(e)
                )
            }
        }

    fun onSendingObserver(onComplete: (SmsSendingService.SendingStatus) -> Unit) =
        object : DisposableCompletableObserver() {
            override fun onError(e: Throwable) {
                onComplete(reportError(e))
            }

            override fun onComplete() {
                onComplete(reportState(SmsSendingService.State.COMPLETED, 0, 0))
            }
        }

    private fun reportState(
        state: SmsSendingService.State,
        sent: Int,
        total: Int
    ): SmsSendingService.SendingStatus {
        val submissionId = smsSender.submissionId
        return SmsSendingService.SendingStatus(submissionId, state, null, sent, total)
    }

    private fun reportError(throwable: Throwable): SmsSendingService.SendingStatus {
        Timber.tag(SmsSendingService::class.java.simpleName).e(throwable)
        val submissionId = smsSender.submissionId
        return SmsSendingService.SendingStatus(
            submissionId,
            SmsSendingService.State.ERROR,
            throwable,
            0,
            0
        )
    }

    fun onSmsNotAccepted(): SmsSendingService.SendingStatus {
        return reportState(SmsSendingService.State.COUNT_NOT_ACCEPTED, 0, 0)
    }
}
