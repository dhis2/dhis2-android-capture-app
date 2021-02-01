package org.dhis2.usescases.teiDashboard.dashboardsfragments.systemInfo

import java.util.Date

data class SystemInfo(
    val serverDate: Date,
    val dateFormat: String,
    val version: String,
    val contextPath: String,
    val systemName: String
)