package org.dhis2.mobile.sync.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.dhis2.mobile.commons.notifications.NotificationManager
import org.dhis2.mobile.commons.notifications.WorkerNotificationInfo
import org.dhis2.mobile.sync.R
import org.dhis2.mobile.sync.domain.SyncDataSet
import org.dhis2.mobile.sync.domain.SyncDataValue
import org.dhis2.mobile.sync.domain.SyncEvent
import org.dhis2.mobile.sync.domain.SyncProgram
import org.dhis2.mobile.sync.domain.SyncTei
import org.dhis2.mobile.sync.model.GranularSyncType
import org.dhis2.mobile.sync.model.SyncDataValueInput
import org.dhis2.mobile.sync.model.fromName
import org.dhis2.mobile.sync.resources.Res
import org.dhis2.mobile.sync.resources.app_name
import org.dhis2.mobile.sync.resources.syncing_data
import org.jetbrains.compose.resources.getString

private const val DATA_UID = "UID"
private const val DATA_SYNC_TYPE = "SYNC_TYPE"
private const val DATA_ORG_UNIT = "ORG_UNIT"
private const val DATA_ATTR_OPTION_COMBO = "ATTR_OPTION_COMBO"
private const val DATA_PERIOD_ID = "PERIOD_ID"
private const val DATA_CAT_COMBO = "CAT_COMBO"

class GranularSyncWorker internal constructor(
    context: Context,
    workerParams: WorkerParameters,
    private val syncProgram: SyncProgram,
    private val syncTei: SyncTei,
    private val syncEvent: SyncEvent,
    private val syncDataSet: SyncDataSet,
    private val syncDataValue: SyncDataValue,
    private val notificationManager: NotificationManager,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        val uid = inputData.getString(DATA_UID) ?: return Result.failure()

        notificationManager.displayGranularSyncNotification(
            smallIcon = R.drawable.ic_sync,
            contentTitle = getString(Res.string.app_name),
            contentText = getString(Res.string.syncing_data),
        )

        val granularSyncType =
            inputData.getString(DATA_SYNC_TYPE)?.let {
                fromName(it)
            }
        val result =
            when (granularSyncType) {
                GranularSyncType.DataSet -> syncDataSet(uid)
                GranularSyncType.DataValue ->
                    syncDataValue(
                        SyncDataValueInput(
                            dataSetUid = uid,
                            orgUnitUid =
                                inputData.getString(DATA_ORG_UNIT)
                                    ?: error("Non null org unit uid required"),
                            periodId =
                                inputData.getString(DATA_PERIOD_ID)
                                    ?: error("Non null period id required"),
                            attrOptionComboUid =
                                inputData.getString(DATA_ATTR_OPTION_COMBO)
                                    ?: error("Non null attr option combo required"),
                            categoryOptionComboUid =
                                inputData.getStringArray(DATA_CAT_COMBO)?.toList()
                                    ?: error("Non null category option combo required"),
                        ),
                    )

                GranularSyncType.Event -> syncEvent(uid)
                GranularSyncType.Program -> syncProgram(uid)
                GranularSyncType.Tei -> syncTei(uid)
                null -> null
            }

        notificationManager.cancelGranularSyncNotification()

        return when {
            result?.isSuccess == true -> Result.success()
            else -> Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationModel =
            notificationManager.getGranularSyncNotification(
                smallIcon = R.drawable.ic_sync,
                contentTitle = getString(Res.string.app_name),
                contentText = getString(Res.string.syncing_data),
            )
        val notificationInfo =
            notificationModel as? WorkerNotificationInfo
                ?: throw IllegalStateException(
                    "Expected WorkerNotificationInfo but got ${notificationModel::class.qualifiedName}",
                )
        return notificationInfo.foregroundInfo
    }

    companion object {
        fun buildInputData(
            uid: String,
            granularSyncType: GranularSyncType,
        ) = Data
            .Builder()
            .putString(DATA_UID, uid)
            .putString(DATA_SYNC_TYPE, granularSyncType.name)
            .build()

        fun buildInputData(
            uid: String,
            granularSyncType: GranularSyncType,
            orgUnitUid: String,
            periodId: String,
            attrOptionComboUid: String,
            catOptionCombo: List<String>,
        ) = Data
            .Builder()
            .putString(DATA_UID, uid)
            .putString(DATA_SYNC_TYPE, granularSyncType.name)
            .putString(DATA_ORG_UNIT, orgUnitUid)
            .putString(DATA_PERIOD_ID, periodId)
            .putString(DATA_ATTR_OPTION_COMBO, attrOptionComboUid)
            .putStringArray(DATA_CAT_COMBO, catOptionCombo.toTypedArray())
            .build()
    }
}
