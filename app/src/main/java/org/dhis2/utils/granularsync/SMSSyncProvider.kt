package org.dhis2.utils.granularsync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Single
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.ConflictType
import org.dhis2.commons.sync.SyncContext
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase
import org.hisp.dhis.android.core.systeminfo.SMSVersion

interface SMSSyncProvider {

    val d2: D2
    val syncContext: SyncContext
    val resourceManager: ResourceManager
    var smsSender: SmsSubmitCase

    fun isPlayServicesEnabled(): Boolean
    fun waitForSMSResponse(
        context: Context,
        senderNumber: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        // default behaviour
    }

    fun unregisterSMSReceiver(requireContext: Context) {
        // default behaviour
    }

    fun expectsResponseSMS(): Boolean {
        return d2.smsModule().configCase().smsModuleConfig.blockingGet().isWaitingForResult
    }

    fun getGatewayNumber(): String {
        return d2.smsModule().configCase().smsModuleConfig.blockingGet().gateway
    }

    fun isSMSEnabled(isTrackerSync: Boolean): Boolean {
        val hasCorrectSmsVersion = if (isTrackerSync) {
            d2.systemInfoModule().versionManager().getSmsVersion() == SMSVersion.V2
        } else {
            true
        }

        val smsModuleIsEnabled =
            d2.smsModule().configCase().smsModuleConfig.blockingGet().isModuleEnabled

        return hasCorrectSmsVersion && smsModuleIsEnabled
    }

    fun getConvertTask(): Single<ConvertTaskResult> {
        return when (syncContext.conflictType()) {
            ConflictType.EVENT -> {
                if (d2.eventModule().events().uid(syncContext.recordUid()).blockingGet()
                    .enrollment() == null
                ) {
                    convertSimpleEvent()
                } else {
                    convertTrackerEvent()
                }
            }
            ConflictType.TEI -> {
                if (d2.enrollmentModule().enrollments().uid(syncContext.recordUid())
                    .blockingExists()
                ) {
                    convertEnrollment()
                } else {
                    Single.error(
                        Exception(
                            resourceManager.getString(R.string.granular_sync_enrollments_empty)
                        )
                    )
                }
            }
            ConflictType.DATA_VALUES -> {
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

    fun onSmsNotAccepted(): SmsSendingService.SendingStatus
    fun observeConfirmationNumber(): LiveData<Boolean?> = MutableLiveData(null)

    fun restartSmsSender() {
        smsSender = d2.smsModule().smsSubmitCase()
    }
}
