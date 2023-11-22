package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.mobile.ui.designsystem.component.Coordinates
import org.hisp.dhis.mobile.ui.designsystem.component.InputCoordinate
import org.hisp.dhis.mobile.ui.designsystem.component.InputPolygon

@Composable
fun ProvidePolygon(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
) {
    InputPolygon(
        modifier = modifier,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        polygonAdded = !fieldUiModel.value.isNullOrEmpty(),
        isRequired = fieldUiModel.mandatory,
        onResetButtonClicked = { fieldUiModel.onClear() },
        onUpdateButtonClicked = { fieldUiModel.invokeUiEvent(UiEventType.REQUEST_LOCATION_BY_MAP) },
    )
}

@Composable
fun ProvideInputCoordinate(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    resources: ResourceManager,
    focusRequester: FocusRequester,
) {
    InputCoordinate(
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        coordinates = mapGeometry(fieldUiModel.value, FeatureType.POINT),
        latitudeText = resources.getString(R.string.latitude),
        longitudeText = resources.getString(R.string.longitude),
        addLocationBtnText = resources.getString(R.string.add_location),
        isRequired = fieldUiModel.mandatory,
        modifier = modifier,
        onResetButtonClicked = {
            focusRequester.requestFocus()
            intentHandler.invoke(
                FormIntent.OnSave(
                    uid = fieldUiModel.uid,
                    value = null,
                    valueType = fieldUiModel.valueType,
                ),
            )
        },
        onUpdateButtonClicked = {
            focusRequester.requestFocus()
            uiEventHandler.invoke(
                RecyclerViewUiEvents.RequestLocationByMap(
                    uid = fieldUiModel.uid,
                    featureType = FeatureType.POINT,
                    value = fieldUiModel.value,
                ),
            )
        },
    )
}

fun mapGeometry(value: String?, featureType: FeatureType): Coordinates? {
    return value?.let {
        val geometry = Geometry.builder()
            .coordinates(it)
            .type(featureType)
            .build()

        Coordinates(
            latitude = GeometryHelper.getPoint(geometry)[1],
            longitude = GeometryHelper.getPoint(geometry)[0],
        )
    }
}
