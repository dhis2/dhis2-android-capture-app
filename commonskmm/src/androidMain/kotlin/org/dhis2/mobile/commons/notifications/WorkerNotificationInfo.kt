package org.dhis2.mobile.commons.notifications

import androidx.work.ForegroundInfo

data class WorkerNotificationInfo(
    val foregroundInfo: ForegroundInfo,
) : NotificationModel
