package org.dhis2.data.notifications

import com.google.gson.reflect.TypeToken
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.prefs.Preference
import org.dhis2.usescases.notifications.domain.Notification
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.domain.Ref
import org.dhis2.usescases.notifications.domain.UserGroups
import org.hisp.dhis.android.core.D2
import timber.log.Timber

class NotificationD2Repository(
    private val d2: D2,
    private val preferenceProvider: BasicPreferenceProvider,
    private val notificationsApi: NotificationsApi,
    private val userGroupsApi: UserGroupsApi
) : NotificationRepository {

    override fun sync() {
        try {
            val allNotifications = getAllNotificationsFromRemote()

            val userGroups = getUserGroups()

            val userNotifications =
                getNotificationsForCurrentUser(allNotifications, userGroups.userGroups)

            preferenceProvider.saveAsJson(Preference.NOTIFICATIONS, userNotifications)

            Timber.d("Notifications synced")
            Timber.d("Notifications: $userNotifications")

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun get(): List<Notification> {

        val listStringType = object : TypeToken<List<Notification>>() {}

        return preferenceProvider.getObjectFromJson(
            Preference.NOTIFICATIONS,
            listStringType,
            listOf()
        )
    }

    override fun getById(id: String): Notification? {
        sync()
        val notifications = get()

        return notifications.find { it.id == id }
    }

    override fun save(notification: Notification) {
        val notifications = getAllNotificationsFromRemote()

        val notificationUpdated = notifications.map {
            if (it.id == notification.id) {
                notification
            } else {
                it
            }
        }

        val response = notificationsApi.postData(notificationUpdated).execute()

        if (response.isSuccessful) {
            sync()
        } else {
            Timber.e("Error updating notifications: ${response.errorBody()}")
        }
    }

    private fun getAllNotificationsFromRemote(): List<Notification> {
        val notificationsResponse = notificationsApi.getData().execute()

        return if (notificationsResponse.isSuccessful) {
            notificationsResponse.body() ?: listOf()
        } else {
            Timber.e("Error getting notifications: ${notificationsResponse.errorBody()}")
            emptyList()
        }
    }

    private fun getUserGroups(): UserGroups {
        val userGroupsResponse =
            userGroupsApi.getData(d2.userModule().user().blockingGet()!!.uid()).execute()

        return if (userGroupsResponse.isSuccessful) {
            userGroupsResponse.body() ?: UserGroups(listOf())
        } else {
            Timber.e("Error getting userGroups: ${userGroupsResponse.errorBody()}")
            UserGroups(listOf())
        }
    }

    private fun getNotificationsForCurrentUser(
        allNotifications: List<Notification>,
        userGroups: List<Ref>
    ): List<Notification> {
        val userGroupIds = userGroups.map { it.id }

        val nonReadByUserNotifications = allNotifications.filter { notification ->
            notification.readBy.none { readBy ->
                readBy.id == d2.userModule().user().blockingGet()!!.uid()
            }
        }

        val notificationsByAll = nonReadByUserNotifications.filter { notification ->
            notification.recipients.wildcard == "ALL"
        }

        val notificationsByUserGroup = nonReadByUserNotifications.filter { notification ->
            notification.recipients.userGroups.any { userGroupIds.contains(it.id) }
        }

        val notificationsByUser = nonReadByUserNotifications.filter { notification ->
            notification.recipients.users.any {
                it.id == d2.userModule().user().blockingGet()!!.uid()
            }
        }

        return notificationsByAll + notificationsByUserGroup + notificationsByUser
    }
}

