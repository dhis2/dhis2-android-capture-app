package org.dhis2.form.ui.intent

import org.dhis2.form.mvi.MviIntent
import org.hisp.dhis.android.core.common.ValueType

sealed class FormIntent : MviIntent {

    data class OnFinish(
        val extraData: String? = null
    ) : FormIntent()

    data class OnClear(
        val extraData: String? = null
    ) : FormIntent()

    data class OnFocus(
        val uid: String,
        val value: String?
    ) : FormIntent()

    data class OnNext(
        val uid: String,
        val value: String?,
        val position: Int? = null
    ) : FormIntent()

    data class OnSave(
        val uid: String,
        val value: String?,
        val valueType: ValueType?,
        val fieldMask: String? = null
    ) : FormIntent()

    data class OnTextChange(
        val uid: String,
        val value: String?
    ) : FormIntent()

    data class ClearValue(
        val uid: String
    ) : FormIntent()

    data class SelectLocationFromCoordinates(
        val uid: String,
        val coordinates: String?,
        val extraData: String
    ) : FormIntent()

    data class SelectLocationFromMap(
        val uid: String,
        val featureType: String,
        val coordinates: String?
    ) : FormIntent()

    data class SaveCurrentLocation(
        val uid: String,
        val value: String?,
        val featureType: String
    ) : FormIntent()

    data class OnSection(
        val sectionUid: String
    ) : FormIntent()

    data class OnRequestCoordinates(
        val uid: String
    ) : FormIntent()

    data class OnCancelRequestCoordinates(
        val uid: String
    ) : FormIntent()
}
