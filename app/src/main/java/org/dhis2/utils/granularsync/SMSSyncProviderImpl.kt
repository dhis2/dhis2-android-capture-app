package org.dhis2.utils.granularsync

import io.reactivex.Completable
import io.reactivex.Single
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.SyncContext
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase
import org.hisp.dhis.android.core.sms.domain.repository.SmsRepository
import java.util.Date

class SMSSyncProviderImpl(
    override val d2: D2,
    override val syncContext: SyncContext,
    override val resourceManager: ResourceManager,
) : SMSSyncProvider {

    override var smsSender: SmsSubmitCase = d2.smsModule().smsSubmitCase()

    override fun isPlayServicesEnabled() = false

    override fun sendSms(
        doOnNext: (sendingStatus: SmsSendingService.SendingStatus) -> Unit,
        doOnNewState: (sendingStatus: SmsSendingService.SendingStatus) -> Unit,
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
                    state.total,
                ),
            )
        }
            .ignoreElements()
            .doOnComplete {
                doOnNewState(
                    reportState(SmsSendingService.State.SENT, 0, 0),
                )
            }
            .andThen(d2.smsModule().configCase().getSmsModuleConfig())
            .flatMapCompletable { config ->
                if (config.isWaitingForResult) {
                    doOnNewState(
                        reportState(SmsSendingService.State.WAITING_RESULT, 0, 0),
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
                                            0,
                                        ),
                                    )
                                }
                            }
                        }.doOnComplete {
                            doOnNewState(
                                reportState(SmsSendingService.State.RESULT_CONFIRMED, 0, 0),
                            )
                        }
                } else {
                    Completable.complete()
                }
            }
    }

    override fun convertSimpleEvent(): Single<ConvertTaskResult> {
        return smsSender.convertSimpleEvent(syncContext.recordUid())
            .map { count -> ConvertTaskResult.Count(count) }
    }

    override fun convertTrackerEvent(): Single<ConvertTaskResult> {
        return smsSender.convertTrackerEvent(syncContext.recordUid())
            .map { count -> ConvertTaskResult.Count(count) }
    }

    override fun convertEnrollment(): Single<ConvertTaskResult> {
        return smsSender.convertEnrollment(syncContext.recordUid())
            .map { count -> ConvertTaskResult.Count(count) }
    }

    override fun convertDataSet(): Single<ConvertTaskResult> {
        return with(syncContext as SyncContext.DataSetInstance) {
            smsSender.convertDataSet(dataSetUid, orgUnitUid, periodId, attributeOptionComboUid)
                .map { count -> ConvertTaskResult.Count(count) }
        }
    }

    private fun reportState(
        state: SmsSendingService.State,
        sent: Int,
        total: Int,
    ): SmsSendingService.SendingStatus {
        val submissionId = smsSender.submissionId
        return SmsSendingService.SendingStatus(submissionId, state, null, sent, total)
    }

    override fun onSmsNotAccepted(): SmsSendingService.SendingStatus {
        return reportState(SmsSendingService.State.COUNT_NOT_ACCEPTED, 0, 0)
    }
}
