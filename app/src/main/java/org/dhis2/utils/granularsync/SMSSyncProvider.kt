package org.dhis2.utils.granularsync

import io.reactivex.Completable
import io.reactivex.Single
import java.util.Date
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase
import org.hisp.dhis.android.core.sms.domain.repository.SmsRepository

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

    fun getConvertTask(): Single<Int> {
        return when (conflictType) {
            SyncStatusDialog.ConflictType.EVENT -> smsSender.convertSimpleEvent(recordUid)
            SyncStatusDialog.ConflictType.TEI -> {
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
                } else if (!d2.enrollmentModule().enrollments().byTrackedEntityInstance()
                        .eq(recordUid).blockingIsEmpty()
                ) {
                    smsSender.convertEnrollment(
                        d2.enrollmentModule().enrollments()
                            .byTrackedEntityInstance().eq(recordUid)
                            .one().blockingGet().uid()
                    )
                } else if (d2.enrollmentModule().enrollments().uid(recordUid).blockingExists()) {
                    smsSender.convertEnrollment(recordUid)
                } else {
                    Single.error(Exception(view.emptyEnrollmentError()))
                }
            }
            SyncStatusDialog.ConflictType.DATA_VALUES -> smsSender.convertDataSet(
                recordUid,
                dvOrgUnit,
                dvPeriodId,
                dvAttrCombo
            )
            else -> Single.error(Exception(view.unsupportedTask()))
        }
    }

    fun sendSms(
        doOnNext: (sent:Int, total:Int) -> Unit,
        doOnSent:() -> Unit,
        doOnWaitingResult: () -> Unit,
        doOnTimeOutResult:()-> Unit,
        doOnResult: () -> Unit
    ): Completable {
        val startDate = Date()

        return smsSender.send().doOnNext { state ->
            doOnNext(state.sent,state.total)
        }.ignoreElements().doOnComplete {
            doOnSent()
        }.andThen(
            d2.smsModule().configCase().smsModuleConfig
        ).flatMapCompletable { config ->
            if (config.isWaitingForResult) {
                doOnWaitingResult()
                smsSender.checkConfirmationSms(startDate)
                    .doOnError { throwable ->
                        if (throwable is SmsRepository.ResultResponseException) {
                            val reason = throwable.reason
                            if (reason == SmsRepository.ResultResponseIssue.TIMEOUT) {
                                doOnTimeOutResult()
                            }
                        }
                    }.doOnComplete {
                        doOnResult()
                    }
            } else {
                Completable.complete()
            }
        }
    }
}