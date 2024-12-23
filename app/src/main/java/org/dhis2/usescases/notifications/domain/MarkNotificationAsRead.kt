package org.dhis2.usescases.notifications.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.Date

class MarkNotificationAsRead(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(notificationId: String): Flow<Unit> {
        return notificationRepository.getById(notificationId).flatMapConcat { notification ->
            Timber.d("notification: $notification")

            notification?.let { notification ->
                val user = userRepository.getCurrentUser()

                val readByUsers = notification.readBy.toMutableList()

                readByUsers.add(ReadBy(Date(), user.uid, user.displayName))

                val notificationUpdated = notification.copy(readBy = readByUsers.toList())

                notificationRepository.save(notificationUpdated)
            } ?: flow { emit(Unit) }
        }
    }
}
