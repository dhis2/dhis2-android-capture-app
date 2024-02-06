package org.dhis2.usescases.notifications.domain

interface NotificationRepository {
    fun sync()
    fun get(): List<Notification>
}