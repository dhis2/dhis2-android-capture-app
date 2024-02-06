package org.dhis2.usescases.notifications.presentation

import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.Notification

class NotificationsPresenter(
    private val notificationsView: NotificationsView,
    private val getNotifications: GetNotifications
) {
    fun refresh() {
        val notifications = getNotifications()

        notificationsView.renderNotifications(notifications)
    }
}

interface NotificationsView {
    fun renderNotifications(notifications: List<Notification>)
}
