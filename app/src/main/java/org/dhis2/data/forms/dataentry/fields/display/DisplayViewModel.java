package org.dhis2.data.forms.dataentry.fields.display;


import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;

import javax.annotation.Nonnull;

@AutoValue
public abstract class DisplayViewModel extends FieldViewModel {

    public static DisplayViewModel create(String id, String label, String value, String description) {
        return new AutoValue_DisplayViewModel(id, label, false, value, null, null, false, null, null, null,  description, ObjectStyle.builder().build(), null);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_DisplayViewModel(uid(), label(), true, value(), null, null, false, null, warning(), error(),   description(),null, null);
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_DisplayViewModel(uid(), label(), mandatory(), value(), null, null, false, null, warning, error(),   description(),null, null);
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_DisplayViewModel(uid(), label(), mandatory(), value(), null, null, false, null, warning(), error,   description(),null, null);
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_DisplayViewModel(uid(), label(), mandatory(), data, null, null, false, null, warning(), error(),   description(),null, null);
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_DisplayViewModel(uid(), label(), mandatory(), value(), null, null, isEditable, null, warning(), error(),   description(),null, null);
    }
}
