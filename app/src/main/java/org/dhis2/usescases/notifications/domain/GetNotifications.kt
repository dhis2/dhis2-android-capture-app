package org.dhis2.usescases.notifications.domain

class GetNotifications (private val notificationRepository: NotificationRepository ) {
    operator fun invoke(): List<Notification>{
        return notificationRepository.get()
    }
}