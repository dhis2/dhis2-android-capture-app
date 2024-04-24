package org.dhis2.usescases.notifications.domain

interface UserRepository {
    fun getCurrentUser(): User
}