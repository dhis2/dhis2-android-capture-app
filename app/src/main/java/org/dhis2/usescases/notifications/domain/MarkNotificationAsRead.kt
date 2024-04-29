package org.dhis2.usescases.notifications.domain

import timber.log.Timber
import java.util.Date

class MarkNotificationAsRead(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(notificationId: String) {
        val notification = notificationRepository.getById(notificationId)

        Timber.d("notification: $notification")


        if (notification != null) {
            val user = userRepository.getCurrentUser()

            val readByUsers = notification.readBy.toMutableList()

            readByUsers.add(ReadBy(Date(), user.uid, user.displayName))

            val notificationUpdated = notification.copy(readBy = readByUsers.toList())

            notificationRepository.save(notificationUpdated)
        }
    }
}