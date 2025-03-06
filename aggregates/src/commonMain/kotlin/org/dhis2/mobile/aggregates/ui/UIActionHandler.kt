package org.dhis2.mobile.aggregates.ui

internal interface UIActionHandler {
    fun onCaptureCoordinates(
        fieldUid: String,
        locationType: String,
        initialData: String?,
        programUid: String?,
        callback: (result: String?) -> Unit,
    )
}
