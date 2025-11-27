package org.dhis2.form.ui.event

import android.content.Intent
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiEventType.OPEN_CUSTOM_INTENT
import org.dhis2.form.model.UiEventType.OPEN_FILE
import org.dhis2.form.model.UiEventType.REQUEST_LOCATION_BY_MAP
import org.dhis2.form.model.UiEventType.SHARE_IMAGE
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import timber.log.Timber

class UiEventFactoryImpl(
    val uid: String,
    val label: String,
    val description: String?,
    val valueType: ValueType,
    val allowFutureDates: Boolean?,
    val optionSet: String?,
) : UiEventFactory {
    override fun generateEvent(
        value: String?,
        uiEventType: UiEventType?,
        renderingType: UiRenderType?,
        fieldUiModel: FieldUiModel,
    ): RecyclerViewUiEvents? {
        var uiEvent: RecyclerViewUiEvents? = null
        try {
            uiEvent =
                when (uiEventType) {
                    REQUEST_LOCATION_BY_MAP ->
                        RecyclerViewUiEvents.RequestLocationByMap(
                            uid = uid,
                            featureType = getFeatureType(renderingType),
                            value = value,
                        )
                    OPEN_FILE -> RecyclerViewUiEvents.OpenFile(fieldUiModel)
                    SHARE_IMAGE ->
                        RecyclerViewUiEvents.OpenChooserIntent(
                            Intent.ACTION_SEND,
                            fieldUiModel.displayName,
                            uid,
                        )

                    OPEN_CUSTOM_INTENT ->
                        RecyclerViewUiEvents.LaunchCustomIntent(
                            fieldUiModel.customIntent,
                            uid,
                        )

                    else -> null
                }
        } catch (e: Exception) {
            Timber.d("wrong format")
        }

        return uiEvent
    }

    private fun getFeatureType(renderingType: UiRenderType?): FeatureType =
        when (renderingType) {
            UiRenderType.DEFAULT -> FeatureType.NONE
            UiRenderType.POINT -> FeatureType.POINT
            UiRenderType.POLYGON -> FeatureType.POLYGON
            UiRenderType.MULTI_POLYGON -> FeatureType.MULTI_POLYGON
            else -> FeatureType.NONE
        }
}
