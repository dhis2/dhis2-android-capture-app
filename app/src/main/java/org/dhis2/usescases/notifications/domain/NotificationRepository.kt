package org.dhis2.usescases.notifications.domain

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun sync(): Flow<Unit>
    fun get(): Flow<List<Notification>>
    fun getById(id: String): Flow<Notification?>
    fun save(notification: Notification): Flow<Unit>
}