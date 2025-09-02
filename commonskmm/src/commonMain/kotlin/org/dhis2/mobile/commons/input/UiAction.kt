package org.dhis2.mobile.commons.input

sealed class UiAction(
    open val id: String,
) {
    data class OnNextClick(
        override val id: String,
    ) : UiAction(id)

    data class OnDoneClick(
        override val id: String,
    ) : UiAction(id)

    data class OnFocusChanged(
        override val id: String,
        val hasFocus: Boolean,
    ) : UiAction(id)

    data class OnValueChanged(
        override val id: String,
        val newValue: String?,
        val showInputDialog: Boolean = true,
    ) : UiAction(id)

    data class OnCaptureCoordinates(
        override val id: String,
        val locationType: String,
        val initialData: String?,
    ) : UiAction(id)

    data class OnEmailAction(
        override val id: String,
        val email: String,
    ) : UiAction(id)

    data class OnSelectFile(
        override val id: String,
    ) : UiAction(id)

    data class OnShareImage(
        override val id: String,
        val filePath: String?,
    ) : UiAction(id)

    data class OnDownloadFile(
        override val id: String,
        val filePath: String?,
    ) : UiAction(id)

    data class OnAddImage(
        override val id: String,
    ) : UiAction(id)

    data class OnTakePhoto(
        override val id: String,
    ) : UiAction(id)

    data class OnCall(
        override val id: String,
        val phoneNumber: String,
    ) : UiAction(id)

    data class OnLinkClicked(
        override val id: String,
        val link: String,
    ) : UiAction(id)

    data class OnOpenOrgUnitTree(
        override val id: String,
        val currentOrgUnitUid: String?,
    ) : UiAction(id)

    data class OnFetchOptions(
        override val id: String,
    ) : UiAction(id)

    data class OnBarCodeScan(
        override val id: String,
        val optionSet: String? = null,
    ) : UiAction(id)

    data class OnQRCodeScan(
        override val id: String,
        val optionSet: String? = null,
    ) : UiAction(id)
}
