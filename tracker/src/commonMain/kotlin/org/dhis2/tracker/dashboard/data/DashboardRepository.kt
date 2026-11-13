package org.dhis2.tracker.dashboard.data

interface DashboardRepository {
    suspend fun enrollmentCategoryComboLabel(programUid: String): String?

    suspend fun enrollmentCategoryOptionComboLabel(enrollmentAttributeOptionCombo: String): String
}
