package org.dhis2.usescases.notifications.domain

import java.util.Date

data class Notification(
    val content: String,
    val createdAt: Date,
    val id: String,
    val readBy: List<ReadBy>,
    val recipients: Recipients
)

data class ReadBy(
    val date: Date,
    val id: String,
    val name: String
)

data class Recipients(
    val userGroups: ArrayList<Ref>,
    val users: List<Ref>,
    val wildcard: String
)

data class Ref(
    val id: String,
    val name: String?
)

data class UserGroups(
    val userGroups: List<Ref>,
)