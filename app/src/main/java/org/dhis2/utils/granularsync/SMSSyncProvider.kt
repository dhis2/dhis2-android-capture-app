package org.dhis2.utils.granularsync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableSingleObserver
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase
import org.hisp.dhis.android.core.systeminfo.SMSVersion

interface SMSSyncProvider {

    val d2: D2
    val conflictType: SyncStatusDialog.ConflictType
    val recordUid: String
    val dvOrgUnit: String?
    val dvAttrCombo: String?
    val dvPeriodId: String?
    val resourceManager: ResourceManager
    val smsSender: SmsSubmitCase

    fun isPlayServicesEnabled(): Boolean
    fun waitForSMSResponse(
        context: Context,
        senderNumber: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    )

    fun unregisterSMSReceiver(requireContext: Context) {}

    fun expectsResponseSMS(): Boolean {
        return d2.smsModule().configCase().smsModuleConfig.blockingGet().isWaitingForResult
    }

    fun getGatewayNumber(): String {
        return d2.smsModule().configCase().smsModuleConfig.blockingGet().gateway
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

    fun getConvertTask(): Single<ConvertTaskResult> {
        return when (conflictType) {
            SyncStatusDialog.ConflictType.EVENT -> {
                if (d2.eventModule().events().uid(recordUid).blockingGet().enrollment() == null) {
                    convertSimpleEvent()
                } else {
                    convertTrackerEvent()
                }
            }
            SyncStatusDialog.ConflictType.TEI -> {
                if (d2.enrollmentModule().enrollments().uid(recordUid).blockingExists()) {
                    convertEnrollment()
                } else {
                    Single.error(
                        Exception(
                            resourceManager.getString(R.string.granular_sync_enrollments_empty)
                        )
                    )
                }
            }
            SyncStatusDialog.ConflictType.DATA_VALUES -> {
                convertDataSet()
            }
            else -> Single.error(
                Exception(
                    resourceManager.getString(R.string.granular_sync_unsupported_task)
                )
            )
        }
    }

    fun convertSimpleEvent(): Single<ConvertTaskResult>
    fun convertTrackerEvent(): Single<ConvertTaskResult>
    fun convertEnrollment(): Single<ConvertTaskResult>
    fun convertDataSet(): Single<ConvertTaskResult>

    fun sendSms(
        doOnNext: (sendingStatus: SmsSendingService.SendingStatus) -> Unit,
        doOnNewState: (sendingStatus: SmsSendingService.SendingStatus) -> Unit
    ): Completable = Completable.complete()

    fun onConvertingObserver(
        onComplete: (SmsSendingService.SendingStatus) -> Unit
    ): DisposableSingleObserver<ConvertTaskResult>

    fun onSendingObserver(
        onComplete: (SmsSendingService.SendingStatus) -> Unit
    ): DisposableCompletableObserver

    fun onSmsNotAccepted(): SmsSendingService.SendingStatus
    fun observeConfirmationNumber(): LiveData<Boolean?> = MutableLiveData(null)
}
