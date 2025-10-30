package org.dhis2.mobile.commons.reporting

interface AnalyticActions {
    fun setEvent(
        param: String,
        value: String,
        event: String,
    )

    fun trackMatomoEvent(
        category: String,
        action: String,
        label: String,
    )

    fun updateMatomoSecondaryTracker(
        matomoUrl: String,
        matomoID: Int,
        trackerName: String,
    )

    fun clearMatomoSecondaryTracker()
}
