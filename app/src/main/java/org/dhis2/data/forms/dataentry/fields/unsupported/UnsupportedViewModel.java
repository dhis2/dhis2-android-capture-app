package org.dhis2.data.forms.dataentry.fields.unsupported;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;

import javax.annotation.Nonnull;

@AutoValue
public abstract class UnsupportedViewModel extends FieldViewModel {
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle) {
        return new AutoValue_UnsupportedViewModel(id, label, false, value, section, null, false, null, null, null, description, objectStyle, null);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null);
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error, description(), objectStyle(), null);
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning, error(), description(), objectStyle(), null);
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, data, programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null);
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null);
    }
}
