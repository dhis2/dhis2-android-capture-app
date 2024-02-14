package org.dhis2.usescases.notifications.domain

interface NotificationRepository {
    fun sync()
    fun get(): List<Notification>
    fun getById(id:String): Notification?
    fun save(notification: Notification)
}