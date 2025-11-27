package org.dhis2.form.ui.intent

import org.hisp.dhis.android.core.common.ValueType

sealed class FormIntent {
    data class OnFinish(
        val extraData: String? = null,
    ) : FormIntent()

    data class OnFocus(
        val uid: String,
        val value: String?,
    ) : FormIntent()

    data class OnNext(
        val uid: String,
        val value: String?,
        val position: Int? = null,
    ) : FormIntent()

    data class OnSave(
        val uid: String,
        val value: String?,
        val valueType: ValueType?,
        val fieldMask: String? = null,
        val allowFutureDates: Boolean? = false,
    ) : FormIntent()

    data class OnSaveCustomIntent(
        val uid: String,
        val value: String?,
        val error: Boolean,
    ) : FormIntent()

    data class OnQrCodeScanned(
        val uid: String,
        val value: String?,
        val valueType: ValueType,
    ) : FormIntent()

    data class OnStoreFile(
        val uid: String,
        val filePath: String,
        val valueType: ValueType?,
    ) : FormIntent()

    data class OnTextChange(
        val uid: String,
        val value: String?,
        val valueType: ValueType?,
    ) : FormIntent()

    data class ClearValue(
        val uid: String,
    ) : FormIntent()

    data class SelectLocationFromCoordinates(
        val uid: String,
        val coordinates: String?,
        val extraData: String,
    ) : FormIntent()

    data class SelectLocationFromMap(
        val uid: String,
        val featureType: String,
        val coordinates: String?,
    ) : FormIntent()

    data class SaveCurrentLocation(
        val uid: String,
        val value: String?,
        val featureType: String,
    ) : FormIntent()

    data class OnSection(
        val sectionUid: String,
    ) : FormIntent()

    data class OnFieldLoadingData(
        val uid: String,
    ) : FormIntent()

    data class OnFieldFinishedLoadingData(
        val uid: String,
    ) : FormIntent()

    data class OnAddImageFinished(
        val uid: String,
    ) : FormIntent()

    data class OnSaveDate(
        val uid: String,
        val value: String?,
        val valueType: ValueType?,
        val allowFutureDates: Boolean = true,
    ) : FormIntent()

    data class FetchOptions(
        val uid: String,
        val optionSetUid: String,
        val value: String?,
    ) : FormIntent()
}
