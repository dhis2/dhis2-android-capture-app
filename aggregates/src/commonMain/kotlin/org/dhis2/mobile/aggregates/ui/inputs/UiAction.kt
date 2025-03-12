package org.dhis2.mobile.aggregates.ui.inputs

sealed class UiAction(open val cellId: String) {
    data class OnNextClick(
        override val cellId: String,
    ) : UiAction(cellId)

    data class OnFocusChanged(
        override val cellId: String,
        val hasFocus: Boolean,
    ) : UiAction(cellId)

    data class OnValueChanged(
        override val cellId: String,
        val newValue: String?,
    ) : UiAction(cellId)

    data class OnCaptureCoordinates(
        override val cellId: String,
        val locationType: String,
        val initialData: String?,

    ) : UiAction(cellId)

    data class OnEmailAction(
        override val cellId: String,
        val email: String,
    ) : UiAction(cellId)

    data class OnSelectFile(
        override val cellId: String,
    ) : UiAction(cellId)

    data class OnOpenFile(
        override val cellId: String,
    ) : UiAction(cellId)

    data class OnShareImage(
        override val cellId: String,
    ) : UiAction(cellId)

    data class OnDownloadImage(
        override val cellId: String,
    ) : UiAction(cellId)

    data class OnAddImage(
        override val cellId: String,
    ) : UiAction(cellId)

    data class OnCall(
        override val cellId: String,
        val phoneNumber: String,
    ) : UiAction(cellId)

    data class OnLinkClicked(
        override val cellId: String,
        val link: String,
    ) : UiAction(cellId)

    data class OnOpenOrgUnitTree(
        override val cellId: String,
        val currentOrgUnitUid: String?,
    ) : UiAction(cellId)
}
