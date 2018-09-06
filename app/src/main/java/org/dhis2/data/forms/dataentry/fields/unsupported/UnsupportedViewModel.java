package org.dhis2.data.forms.dataentry.fields.unsupported;

import android.support.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UnsupportedViewModel extends FieldViewModel {
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable) {
        return new AutoValue_UnsupportedViewModel(id, label, false, value, section, null, editable, null, null, null);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error);
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning, error());
    }
}
