package org.dhis2.usescases.notifications.domain

import kotlinx.coroutines.flow.Flow

class GetNotifications(private val notificationRepository: NotificationRepository) {
    operator fun invoke(): Flow<List<Notification>> {
        return notificationRepository.get()
    }
}
