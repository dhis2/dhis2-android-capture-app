package com.dhis2.data.forms.dataentry.fields.old_fields.daterow;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.dataentry.form.dataentry.fields.EditableFieldViewModel;

@AutoValue
public abstract class DateViewModel extends EditableFieldViewModel<String> {

    @NonNull
    public abstract String hint();

    @NonNull
    public abstract Boolean isDateTime();

    @NonNull
    public static DateViewModel forDate(@NonNull String uid, @NonNull String label,
                                        @NonNull String hint, @NonNull Boolean mandatory, @Nullable String value) {
        return new AutoValue_DateViewModel(uid, label, mandatory, value, hint, false);
    }

    @NonNull
    public static DateViewModel forDateTime(@NonNull String uid, @NonNull String label,
                                            @NonNull String hint, @NonNull Boolean mandatory, @Nullable String value) {
        return new AutoValue_DateViewModel(uid, label, mandatory, value, hint, true);
    }
}
