package com.dhis2.data.forms.dataentry.fields.old_fields.optionsrow;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.dataentry.form.dataentry.fields.EditableFieldViewModel;

@AutoValue
public abstract class OptionsViewModel extends EditableFieldViewModel<String> {

    @NonNull
    public abstract String hint();

    @NonNull
    public abstract String optionSet();

    @NonNull
    public static OptionsViewModel create(@NonNull String uid, @NonNull String label, @NonNull String hint,
                                          @NonNull Boolean mandatory, @NonNull String optionSet, @Nullable String value) {
        return new AutoValue_OptionsViewModel(uid, label, mandatory, value, hint, optionSet);
    }
}
