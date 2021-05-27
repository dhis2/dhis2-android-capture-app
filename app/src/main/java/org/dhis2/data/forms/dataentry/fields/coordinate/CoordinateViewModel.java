package org.dhis2.data.forms.dataentry.fields.coordinate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;

import com.google.auto.value.AutoValue;

import org.dhis2.Bindings.DoubleExtensionsKt;
import org.dhis2.Bindings.StringExtensionsKt;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.ActionType;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.ui.style.FormUiModelStyle;
import org.dhis2.uicomponents.map.geometry.LngLatValidatorKt;
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.ObjectStyle;

import io.reactivex.processors.FlowableProcessor;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static android.text.TextUtils.isEmpty;

@AutoValue
public abstract class CoordinateViewModel extends FieldViewModel {

    private final ObservableField<String> observableErrorMessage = new ObservableField<>();
    private final ObservableField<String> observableWarningMessage = new ObservableField<>();
    private final ObservableField<String> latitudeValue = new ObservableField<>();
    private final ObservableField<String> longitudeValue = new ObservableField<>();

    @Nullable
    public abstract FeatureType featureType();

    public abstract boolean isBackgroundTransparent();

    public static CoordinateViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, FeatureType featureType, boolean isBackgroundTransparent, boolean isSearchMode, FlowableProcessor<RowAction> processor, FormUiModelStyle style) {
        return new AutoValue_CoordinateViewModel(id, label, mandatory, value, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.COORDINATES, processor, style, false, featureType, isBackgroundTransparent, isSearchMode);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_CoordinateViewModel(uid(), label(), true, value(), programStageSection(), null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.COORDINATES, processor(), style(),activated(), featureType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.COORDINATES, processor(), style(), activated(), featureType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.COORDINATES, processor(), style(), activated(), featureType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), data, programStageSection(), null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.COORDINATES, processor(), style(), activated(), featureType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, isEditable, null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.COORDINATES, processor(), style(), activated(), featureType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.COORDINATES, processor(), style(), isFocused, featureType(), isBackgroundTransparent(), isSearchMode());
    }

    @Override
    public int getLayoutId() {
        return R.layout.custom_form_coordinate;
    }

    public void onCurrentLocationClick(Geometry geometry) {
        if (callback == null) return;
        callback.onItemAction(new RowAction(
                uid(),
                geometry == null ? null : geometry.coordinates(),
                false,
                null,
                null,
                featureType().name(),
                null,
                ActionType.ON_SAVE
        ));
    }

    public abstract boolean isSearchMode();

    public ObservableField<String> observeWarningMessage() {
        observableWarningMessage.set(warning());
        return observableWarningMessage;
    }

    public ObservableField<String> observeErrorMessage() {
        observableErrorMessage.set(error());
        return observableErrorMessage;
    }

    public void onLatOrLogFocusChanged(boolean hasFocus,
                                       String coordinateValue,
                                       String errorMessage,
                                       Function1<Double, Boolean> coordinateValidator,
                                       Function1<String, Unit> callback){
        if (!hasFocus) {
            if (!coordinateValue.isEmpty()) {
                Double coordinateString = Double.valueOf(StringExtensionsKt.parseToDouble(coordinateValue));
                Double coordinate = DoubleExtensionsKt.truncate(coordinateString);
                String error;
                if (!coordinateValidator.invoke(coordinate)) {
                    error = errorMessage;
                } else {
                    error = null;
                }
                callback.invoke(coordinate.toString());
                observableErrorMessage.set(error);
            }
        } else {
            onItemClick();
        }
    }

    public boolean isPoint() {
        return featureType() == FeatureType.POINT;
    }

    public void onDescriptionClick() {
        callback.showDialog(label(), description());
    }

    public Geometry currentGeometry() {
        if (value() != null) {
            return Geometry.builder()
                    .coordinates(value())
                    .type(featureType())
                    .build();
        } else {
            return null;
        }
    }

    public boolean allowClearValue() {
        return currentGeometry() != null && editable();
    }

    public void onClearValueClick() {
        if (!activated()) {
            onItemClick();
        }
        onCurrentLocationClick(null);
    }

    public boolean validateFilledCoordinates() {
        return (!isEmpty(latitudeValue.get()) && !isEmpty(longitudeValue.get())) ||
                (isEmpty(latitudeValue.get()) && isEmpty(longitudeValue.get()));
    }

    public void onLatitudeChanged(CharSequence s, int start, int before, int count) {
        String latitude = s.toString();
        latitudeValue.set(latitude.isEmpty() ? null : latitude);
    }

    public void onLongitudeChanged(CharSequence s, int start, int before, int count) {
        String longitude = s.toString();
        longitudeValue.set(longitude.isEmpty() ? null : longitude);
    }

    public boolean onFilledCoordinate(String coordinatesErrorMessage) {
        if (validateFilledCoordinates()) {
            Double lat = parseCoordinateStringToDouble(latitudeValue.get());
            Double lon = parseCoordinateStringToDouble(longitudeValue.get());
            if (lat != null && lon != null) {
                if (LngLatValidatorKt.areLngLatCorrect(lat, lon)) {
                    onCurrentLocationClick(GeometryHelper.createPointGeometry(lon, lat));
                } else {
                    observableErrorMessage.set(coordinatesErrorMessage);
                }
            } else {
                onCurrentLocationClick(null);
            }
            return true;
        } else {
            return false;
        }
    }

    private Double parseCoordinateStringToDouble(String coordinateValue) {
        if (coordinateValue == null) {
            return null;
        } else {
            return Double.valueOf(StringExtensionsKt.parseToDouble(coordinateValue));
        }
    }

    public void requestCurrentLocation() {
        callback.currentLocation(uid());
    }

    public void requestMapLocation() {
        callback.mapRequest(uid(), featureType().name(), value());
    }
}
