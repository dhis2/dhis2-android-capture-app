package org.dhis2.usescases.notifications.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.Notification

class NotificationsPresenter(
    private val notificationsView: NotificationsView,
    private val getNotifications: GetNotifications,
    private val markNotificationAsRead: MarkNotificationAsRead
) {
    fun refresh() {
        CoroutineScope(Dispatchers.Main).launch {
            getNotifications().collect {
                notificationsView.renderNotifications(it)
            }
        }
    }

    fun markNotificationAsRead(notification: Notification) {
        CoroutineScope(Dispatchers.IO).launch {
            markNotificationAsRead.invoke(notification.id).collect {}
        }
    }
}

interface NotificationsView {
    fun renderNotifications(notifications: List<Notification>)
}

