package org.dhis2.mobile.aggregates.ui.inputs

sealed class UiAction {
    data object OnNextClick : UiAction()
    data class OnFocusChanged(
        val hasFocus: Boolean,
    ) : UiAction()

    data class OnValueChanged(
        val cellId: String,
        val newValue: String?,
    ) : UiAction()

    data class OnCaptureCoordinates(
        val cellId: String,
    ) : UiAction()

    data class OnDateTimeAction(
        val cellId: String,
        val currentValue: String,
    ) : UiAction()

    data class OnEmailAction(
        val cellId: String,
        val email: String,
    ) : UiAction()

    data class OnSelectFile(
        val cellId: String,
    ) : UiAction()

    data class OnOpenFile(
        val cellId: String,
    ) : UiAction()

    data class OnShareImage(
        val cellId: String,
    ) : UiAction()

    data class OnDownloadImage(
        val cellId: String,
    ) : UiAction()

    data class OnAddImage(
        val cellId: String,
    ) : UiAction()

    data class OnCall(
        val cellId: String,
        val phoneNumber: String,
    ) : UiAction()

    data class OnLinkClicked(
        val cellId: String,
        val link: String,
    ) : UiAction()
}
