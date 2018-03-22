package com.dhis2.data.forms.dataentry.fields.spinner;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

/**
 * Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class SpinnerViewModel extends FieldViewModel {

    @NonNull
    public abstract String uid();

    @NonNull
    public abstract String label();

    @NonNull
    public abstract Boolean mandatory();

    @Nullable
    public abstract String value();

    @NonNull
    public abstract String hint();

    @NonNull
    public abstract String optionSet();

    public static FieldViewModel create(String id, String label, String hintFilterOptions, Boolean mandatory, String optionSet, String value) {
        return new AutoValue_SpinnerViewModel(id, label, mandatory, value, hintFilterOptions, optionSet);
    }
}
