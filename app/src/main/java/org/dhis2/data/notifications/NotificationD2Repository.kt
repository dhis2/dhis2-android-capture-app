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
    private val notificationsDataStoreApi: NotificationsApi,
    private val userGroupsApi: UserGroupsApi
) : NotificationRepository {

    override fun sync() {
        try {
            val notificationsResponse = notificationsDataStoreApi.getData().execute()

            val userGroupsResponse =
                userGroupsApi.getData(d2.userModule().user().blockingGet().uid()).execute()


            if (notificationsResponse.isSuccessful && userGroupsResponse.isSuccessful) {
                val allNotifications = notificationsResponse.body() ?: listOf()
                val userGroups = userGroupsResponse.body() ?: UserGroups(listOf())

                val userNotifications = getNotificationsForCurrentUser(allNotifications, userGroups.userGroups)

                preferenceProvider.saveAsJson(Preference.NOTIFICATIONS, userNotifications)

                Timber.d("Notifications synced")
                Timber.d("Notifications: $userNotifications")
            } else {
                Timber.e("Error getting notifications: ${notificationsResponse.errorBody()}")
                Timber.e("Error getting userGroups: ${userGroupsResponse.errorBody()}")
            }
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

    private fun getNotificationsForCurrentUser(
        allNotifications: List<Notification>,
        userGroups: List<Ref>
    ): List<Notification> {
        val userGroupIds = userGroups.map { it.id }

        val nonReadByUserNotifications = allNotifications.filter { notification ->
            notification.readBy.none { readBy ->
                readBy.id == d2.userModule().user().blockingGet().uid()
            }
        }

        Timber.d("nonReadByUserNotifications: $nonReadByUserNotifications")

        val notificationsByAll = nonReadByUserNotifications.filter { notification ->
            notification.recipients.wildcard == "ALL"
        }

        Timber.d("notificationsByAll: $notificationsByAll")

        val notificationsByUserGroup = nonReadByUserNotifications.filter { notification ->
            notification.recipients.userGroups.any { userGroupIds.contains(it.id) }
        }

        Timber.d("notificationsByUserGroup: $notificationsByUserGroup")

        val notificationsByUser = nonReadByUserNotifications.filter { notification ->
            notification.recipients.users.any {
                it.id == d2.userModule().user().blockingGet().uid()
            }
        }

        Timber.d("notificationsByUser: $notificationsByUser")

        return notificationsByAll + notificationsByUserGroup + notificationsByUser
    }
}

