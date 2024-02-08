package org.dhis2.data.notifications

import com.google.gson.reflect.TypeToken
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.prefs.Preference
import org.dhis2.usescases.notifications.domain.Notification
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import timber.log.Timber

class NotificationD2Repository(
    private val d2: D2,
    private val preferenceProvider: BasicPreferenceProvider,
    private val notificationsDataStoreApi: NotificationsApi
) : NotificationRepository {

    override fun sync() {
        try {
            val response = notificationsDataStoreApi.getData().execute()

            if (response.isSuccessful) {
                val allNotifications = response.body() ?: listOf()

                val userNotifications = getNotificationsForCurrentUser(allNotifications)

                preferenceProvider.saveAsJson(Preference.NOTIFICATIONS, userNotifications)

                Timber.d("Notifications synced")
                Timber.d(userNotifications.toString())
            } else {
                Timber.e("Error getting notifications: ${response.errorBody()}")
                Timber.e("Error getting notifications: $response")
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

    private fun getNotificationsForCurrentUser(allNotifications: List<Notification>): List<Notification> {
        val nonReadByUserNotifications = allNotifications.filter { notification ->
            notification.readBy.none { readBy ->
                readBy.id == d2.userModule().user().blockingGet().uid()
            }
        }

        val userOrgUnitGroups =
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .withOrganisationUnitGroups()
                .blockingGet().flatMap { ou ->
                    if (ou.organisationUnitGroups() != null) ou.organisationUnitGroups()!!
                        .map { ouGroup -> ouGroup.uid() }
                    else listOf()
                }.distinct()

        val userNotifications = nonReadByUserNotifications.filter { notification ->
            notification.recipients.userGroups.any { userOrgUnitGroups.contains(it.id) } ||
                    notification.recipients.wildcard == "ALL" || notification.recipients.users.any {
                it.id == d2.userModule().user().blockingGet().uid()
            }
        }

        return userNotifications
    }
}

