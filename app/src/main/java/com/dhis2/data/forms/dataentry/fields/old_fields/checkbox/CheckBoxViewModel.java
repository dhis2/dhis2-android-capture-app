package com.dhis2.data.forms.dataentry.fields.old_fields.checkbox;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.dataentry.form.dataentry.fields.EditableFieldViewModel;

import java.util.Locale;

@AutoValue
public abstract class CheckBoxViewModel extends EditableFieldViewModel<CheckBoxViewModel.Value> {
    public enum Value {
        CHECKED("true"), UNCHECKED("");

        @NonNull
        private final String value;

        Value(@NonNull String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @NonNull
    public static CheckBoxViewModel fromRawValue(@NonNull String id, @NonNull String label,
                                                 @NonNull Boolean mandatory, @Nullable String value) {
        if (value == null) {
            return new AutoValue_CheckBoxViewModel(id, label, mandatory, null);
        } else if (value.toLowerCase(Locale.US).equals(Value.CHECKED.toString())) {
            return new AutoValue_CheckBoxViewModel(id, label, mandatory, Value.CHECKED);
        } else if (value.toLowerCase(Locale.US).equals(Value.UNCHECKED.toString())) {
            return new AutoValue_CheckBoxViewModel(id, label, mandatory, Value.UNCHECKED);
        } else {
            throw new IllegalArgumentException("Unsupported value: " + value);
        }
    }
}