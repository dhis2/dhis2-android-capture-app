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

    fun onSelectFile(
        fieldUid: String,
        callback: (result: String?) -> Unit,
    )
    fun onDownloadFile(
        fieldUid: String,
        filepath: String?,
        callback: (result: String?) -> Unit,
    )
    fun onAddImage(
        fieldUid: String,
        callback: (result: String?) -> Unit,
    )
    fun onTakePicture(callback: (result: String?) -> Unit)

    fun onShareImage(
        filepath: String?,
        onActivityNotFound: () -> Unit,
    )
}

enum class CallbackStatus {
    OK,
    ERROR,
}
