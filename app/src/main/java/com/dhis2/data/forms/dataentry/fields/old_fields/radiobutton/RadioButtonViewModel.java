package com.dhis2.data.forms.dataentry.fields.old_fields.radiobutton;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.dataentry.form.dataentry.fields.EditableFieldViewModel;

import java.util.Locale;

@AutoValue
public abstract class RadioButtonViewModel extends EditableFieldViewModel<RadioButtonViewModel.Value> {
    public enum Value {
        YES("true"), NO("false"), NONE("");

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
    public static RadioButtonViewModel fromRawValue(@NonNull String uid, @NonNull String label,
                                                    @NonNull Boolean mandatory, @Nullable String value) {
        if (value == null) {
            return new AutoValue_RadioButtonViewModel(uid, label, mandatory, null);
        } else if (value.toLowerCase(Locale.US).equals(Value.YES.toString())) {
            return new AutoValue_RadioButtonViewModel(uid, label, mandatory, Value.YES);
        } else if (value.toLowerCase(Locale.US).equals(Value.NO.toString())) {
            return new AutoValue_RadioButtonViewModel(uid, label, mandatory, Value.NO);
        } else if (value.isEmpty()) {
            return new AutoValue_RadioButtonViewModel(uid, label, mandatory, Value.NONE);
        } else {
            throw new IllegalArgumentException("Unsupported value: " + value);
        }
    }
}