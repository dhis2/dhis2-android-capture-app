package org.dhis2.utils.granularsync

import java.util.Date

data class StatusLogItem(
    val date: Date,
    val description: String,
    val openLogs: Boolean,
)
