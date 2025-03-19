package org.dhis2.mobile.aggregates.ui

interface UiActionHandler {
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

    fun onCall(phoneNumber: String, onActivityNotFound: () -> Unit)
    fun onSendEmail(email: String, onActivityNotFound: () -> Unit)
    fun onOpenLink(url: String, onActivityNotFound: () -> Unit)
}
