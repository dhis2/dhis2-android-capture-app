package com.dhis2.data.forms.dataentry.fields.spinner;

import android.support.annotation.NonNull;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

/**
 * Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class SpinnerViewModel extends FieldViewModel {

    @NonNull
    public abstract String hint();

    @NonNull
    public abstract String optionSet();

    public static SpinnerViewModel create(String id, String label, String hintFilterOptions, Boolean mandatory, String optionSet, String value, String section) {
        return new AutoValue_SpinnerViewModel(id, label, mandatory, value,section, null, hintFilterOptions, optionSet);
    }
}
