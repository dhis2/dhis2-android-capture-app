package org.dhis2.data.forms.dataentry.fields.unsupported;

import androidx.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;

@AutoValue
public abstract class UnsupportedViewModel extends FieldViewModel {
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable,String description) {
        return new AutoValue_UnsupportedViewModel(id, label, false, value, section, null, editable, null, null, null,description);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(),description());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error,description());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning, error(),description());
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, data, programStageSection(), allowFutureDate(),false, optionSet(), warning(), error(),description());
    }
}
