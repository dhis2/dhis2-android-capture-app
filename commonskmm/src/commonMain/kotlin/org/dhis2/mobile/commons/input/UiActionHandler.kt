package org.dhis2.mobile.commons.input

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

    fun onCall(
        phoneNumber: String,
        onActivityNotFound: () -> Unit,
    )

    fun onSendEmail(
        email: String,
        onActivityNotFound: () -> Unit,
    )

    fun onOpenLink(
        url: String,
        onActivityNotFound: () -> Unit,
    )

    fun onSelectFile(
        fieldUid: String,
        callback: (result: String?) -> Unit,
        onFailure: () -> Unit,
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

    fun onQRScan(
        fieldUid: String,
        optionSet: String?,
        callback: (result: String?) -> Unit,
    )

    fun onBarcodeScan(
        fieldUid: String,
        optionSet: String?,
        callback: (result: String?) -> Unit,
    )
}

enum class CallbackStatus {
    OK,
    ERROR,
}
