package org.dhis2.data.forms.dataentry.fields.picture;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.ui.intent.FormIntent;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;

@AutoValue
public abstract class PictureViewModel extends FieldViewModel {

    public abstract boolean isBackgroundTransparent();

    public static PictureViewModel create(String id, int layoutId, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, boolean isBackgroundTransparent) {
        return new AutoValue_PictureViewModel(id, layoutId, label, mandatory, value, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.PICTURE, null, null, false, ValueType.IMAGE, isBackgroundTransparent);
    }

    @Override
    public PictureViewModel setMandatory() {
        return new AutoValue_PictureViewModel(uid(), layoutId(), label(), true, value(), programStageSection(), null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, style(), hint(), activated(), valueType(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public PictureViewModel withWarning(@NonNull String warning) {
        return new AutoValue_PictureViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, style(), hint(), activated(), valueType(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public PictureViewModel withError(@NonNull String error) {
        return new AutoValue_PictureViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, style(), hint(), activated(), valueType(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public PictureViewModel withValue(String data) {
        return new AutoValue_PictureViewModel(uid(), layoutId(), label(), mandatory(), data, programStageSection(), null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, style(), hint(), activated(), valueType(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public PictureViewModel withEditMode(boolean isEditable) {
        return new AutoValue_PictureViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(), null, isEditable, null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, style(), hint(), activated(), valueType(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public PictureViewModel withFocus(boolean isFocused) {
        return new AutoValue_PictureViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, style(), hint(), isFocused, valueType(), isBackgroundTransparent());
    }

    public void onClearValue() {
        callback.intent(new FormIntent.OnSave(
                uid(),
                null,
                null,
                fieldMask()
        ));
    }
}
