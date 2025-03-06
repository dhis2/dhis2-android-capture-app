package org.dhis2.mobile.aggregates.ui

internal interface UIActionHandler {
    fun onCaptureCoordinates(
        fieldUid: String,
        locationType: String,
        initialData: String?,
        callback: (result: String?) -> Unit,
    )

    fun onCaptureOrgUnit(
        preselectedOrgUnits: List<String>,
        callback: (result: String?) -> Unit,
    )

    fun onCall(phoneNumber: String)
    fun onSendEmail(email: String)
    fun onOpenLink(url: String)
}
