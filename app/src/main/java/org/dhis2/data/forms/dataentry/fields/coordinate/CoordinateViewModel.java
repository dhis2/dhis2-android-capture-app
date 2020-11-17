package org.dhis2.data.forms.dataentry.fields.coordinate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.ObjectStyle;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class CoordinateViewModel extends FieldViewModel {

    public boolean activated = false;

    @Nullable
    public abstract FeatureType featureType();

    public abstract boolean isBackgroundTransparent();


    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, FeatureType featureType, boolean isBackgroundTransparent, boolean isSearchMode) {
        return new AutoValue_CoordinateViewModel(id, label, mandatory, value, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.COORDINATES, null, featureType, isBackgroundTransparent, isSearchMode);
    }
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, FeatureType featureType, boolean isBackgroundTransparent, boolean isSearchMode, FlowableProcessor<RowAction> processor) {
        return new AutoValue_CoordinateViewModel(id, label, mandatory, value, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.COORDINATES, processor, featureType, isBackgroundTransparent, isSearchMode);
    }
    //FlowableProcessor<RowAction> processor

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_CoordinateViewModel(uid(), label(), true, value(), programStageSection(), null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.COORDINATES, processor() ,featureType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.COORDINATES, processor(), featureType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.COORDINATES, processor(), featureType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), data, programStageSection(), null, false, null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.COORDINATES, processor(), featureType(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, isEditable, null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.COORDINATES, processor(),featureType(), isBackgroundTransparent(), isSearchMode());
    }

    @Override
    public int getLayoutId() {
        return R.layout.custom_form_coordinate;
    }

    public void onCurrentLocationClick(Geometry geometry) {
        if (processor() == null) return;

        processor().onNext(RowAction.create(uid(),
                geometry == null ? null : geometry.coordinates(),
                getAdapterPosition(),
                featureType().name()));
    }

    public void onActivate() {
        activated = true;
    }

    public void onDeactivate() {
        activated = false;
    }

    public abstract boolean isSearchMode();
}
