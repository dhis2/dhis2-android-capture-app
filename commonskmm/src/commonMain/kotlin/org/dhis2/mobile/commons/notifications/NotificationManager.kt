package org.dhis2.mobile.commons.notifications

interface NotificationManager {
    fun getDataSyncNotification(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
        progress: Int,
    ): NotificationModel

    fun displayDataSyncNotification(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
        progress: Int,
    )

    fun getMetadataSyncNotification(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
        progress: Int,
    ): NotificationModel

    fun displayMetadataSyncNotification(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
        progress: Int,
    )

    fun getSyncSettingsNotificationModel(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
    ): NotificationModel

    fun displaySyncSettingsNotification(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
    )

    fun cancelMetadataSyncNotification()

    fun cancelSyncSettingsNotification()

    fun cancelDataSyncNotification()
}
