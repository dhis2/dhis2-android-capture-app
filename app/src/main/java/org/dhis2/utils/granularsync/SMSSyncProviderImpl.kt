package org.dhis2.utils.granularsync

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableSingleObserver
import java.util.Date
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase
import org.hisp.dhis.android.core.sms.domain.repository.SmsRepository
import timber.log.Timber

class SMSSyncProviderImpl(
    override val d2: D2,
    override val conflictType: SyncStatusDialog.ConflictType,
    override val recordUid: String,
    override val dvOrgUnit: String?,
    override val dvAttrCombo: String?,
    override val dvPeriodId: String?,
    override val resourceManager: ResourceManager
) : SMSSyncProvider {

    override val smsSender: SmsSubmitCase = d2.smsModule().smsSubmitCase()

    override fun isPlayServicesEnabled() = false
    override fun waitForSMSResponse(
        context: Context,
        senderNumber: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
    }

    override fun getSSMSIntentFilter(): String? = null
    override fun getSendPermission(): String? = null

    override fun sendSms(
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

    override fun convertSimpleEvent(): Single<ConvertTaskResult> {
        return smsSender.convertSimpleEvent(recordUid)
            .map { count -> ConvertTaskResult.Count(count) }
    }

    override fun convertTrackerEvent(): Single<ConvertTaskResult> {
        return smsSender.convertTrackerEvent(recordUid)
            .map { count -> ConvertTaskResult.Count(count) }
    }

    override fun convertEnrollment(): Single<ConvertTaskResult> {
        return smsSender.convertEnrollment(recordUid)
            .map { count -> ConvertTaskResult.Count(count) }
    }

    override fun convertDataSet(): Single<ConvertTaskResult> {
        return smsSender.convertDataSet(recordUid, dvOrgUnit, dvPeriodId, dvAttrCombo)
            .map { count -> ConvertTaskResult.Count(count) }
    }

    override fun onConvertingObserver(onComplete: (SmsSendingService.SendingStatus) -> Unit) =
        object : DisposableSingleObserver<ConvertTaskResult>() {
            override fun onSuccess(countResult: ConvertTaskResult) {
                if (countResult is ConvertTaskResult.Count) {
                    onComplete(
                        reportState(SmsSendingService.State.CONVERTED, 0, countResult.smsCount)
                    )
                    onComplete(
                        reportState(
                            SmsSendingService.State.WAITING_COUNT_CONFIRMATION,
                            0,
                            countResult.smsCount
                        )
                    )
                }
            }

            override fun onError(e: Throwable) {
                onComplete(
                    reportError(e)
                )
            }
        }

    override fun onSendingObserver(onComplete: (SmsSendingService.SendingStatus) -> Unit) =
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

    override fun onSmsNotAccepted(): SmsSendingService.SendingStatus {
        return reportState(SmsSendingService.State.COUNT_NOT_ACCEPTED, 0, 0)
    }
}
