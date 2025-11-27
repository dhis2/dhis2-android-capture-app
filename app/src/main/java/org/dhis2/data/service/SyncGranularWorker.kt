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

package org.dhis2.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.dhis2.App
import org.dhis2.R
import org.dhis2.commons.Constants.ATTRIBUTE_OPTION_COMBO
import org.dhis2.commons.Constants.CATEGORY_OPTION_COMBO
import org.dhis2.commons.Constants.CONFLICT_TYPE
import org.dhis2.commons.Constants.ORG_UNIT
import org.dhis2.commons.Constants.PERIOD_ID
import org.dhis2.commons.Constants.UID
import org.dhis2.commons.sync.ConflictType
import javax.inject.Inject

private const val GRANULAR_CHANNEL = "sync_granular_notification"
private const val SYNC_GRANULAR_ID = 8071988

class SyncGranularWorker(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    @Inject
    internal lateinit var presenter: SyncPresenter

    override fun doWork(): Result {
        (applicationContext as App)
            .userComponent()
            ?.plus(SyncGranularRxModule())
            ?.inject(this)

        val uid = inputData.getString(UID) ?: return Result.failure()
        val conflictType = inputData.getString(CONFLICT_TYPE)?.let { ConflictType.valueOf(it) }

        triggerNotification(
            title = applicationContext.getString(R.string.app_name),
            content = applicationContext.getString(R.string.syncing_data),
            progress = 0,
        )

        val result =
            when (conflictType) {
                ConflictType.PROGRAM -> {
                    presenter.blockSyncGranularProgram(uid)
                }
                ConflictType.TEI -> {
                    presenter.blockSyncGranularTei(uid)
                }
                ConflictType.EVENT -> {
                    presenter.blockSyncGranularEvent(uid)
                }
                ConflictType.DATA_SET -> {
                    presenter.blockSyncGranularDataSet(
                        uid,
                    )
                }
                ConflictType.DATA_VALUES ->
                    presenter.blockSyncGranularDataValues(
                        uid,
                        inputData.getString(ORG_UNIT) as String,
                        inputData.getString(ATTRIBUTE_OPTION_COMBO) as String,
                        inputData.getString(PERIOD_ID) as String,
                        inputData.getStringArray(CATEGORY_OPTION_COMBO) as Array<String>,
                    )
                else -> Result.failure()
            }

        cancelNotification()
        return result
    }

    private fun triggerNotification(
        title: String,
        content: String,
        progress: Int,
    ) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel =
                NotificationChannel(
                    GRANULAR_CHANNEL,
                    "GranularSync",
                    NotificationManager.IMPORTANCE_HIGH,
                )
            notificationManager.createNotificationChannel(mChannel)
        }
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat
                .Builder(
                    applicationContext,
                    GRANULAR_CHANNEL,
                ).setSmallIcon(R.drawable.ic_sync)
                .setContentTitle(title)
                .setContentText(content)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setProgress(100, progress, true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        setForegroundAsync(
            ForegroundInfo(
                SYNC_GRANULAR_ID,
                notificationBuilder.build(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0,
            ),
        )
    }

    private fun cancelNotification() {
        val notificationManager =
            NotificationManagerCompat.from(
                applicationContext,
            )
        notificationManager.cancel(SYNC_GRANULAR_ID)
    }
}
