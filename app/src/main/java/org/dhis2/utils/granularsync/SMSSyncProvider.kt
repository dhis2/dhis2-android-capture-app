package org.dhis2.utils.granularsync

import android.content.Context
import com.google.android.gms.tasks.Task
import io.reactivex.Single
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
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
    fun getTaskOrNull(context: Context, senderNumber: String): Task<Void>?

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
    fun getConvertTask(): Single<String> {
        return when (conflictType) {
            SyncStatusDialog.ConflictType.EVENT -> {
                if (d2.eventModule().events().uid(recordUid).blockingGet().enrollment() == null) {
                    smsSender.compressSimpleEvent(recordUid)
                } else {
                    smsSender.compressTrackerEvent(recordUid)
                }
            }
            SyncStatusDialog.ConflictType.TEI -> {
                if (d2.enrollmentModule().enrollments().uid(recordUid).blockingExists()) {
                    smsSender.compressEnrollment(recordUid)
                } else {
                    Single.error(
                        Exception(
                            resourceManager.getString(R.string.granular_sync_enrollments_empty)
                        )
                    )
                }
            }
            SyncStatusDialog.ConflictType.DATA_VALUES -> smsSender.compressDataSet(
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
}
