package com.dhis2.data.forms.dataentry.fields.radiobutton;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.forms.dataentry.fields.EditableFieldViewModel;
import com.google.auto.value.AutoValue;

import java.util.Locale;

/**
 * Created by frodriguez on 1/24/2018.
 */
@AutoValue
public abstract class RadioButtonViewModel extends EditableFieldViewModel<RadioButtonViewModel> {
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
            return new AutoValue_RadioButtonViewModel(uid, label, mandatory, null);
        } else if (value.toLowerCase(Locale.US).equals(Value.NO.toString())) {
            return new AutoValue_RadioButtonViewModel(uid, label, mandatory, null);
        } else if (value.isEmpty()) {
            return new AutoValue_RadioButtonViewModel(uid, label, mandatory, null);
        } else {
            throw new IllegalArgumentException("Unsupported value: " + value);
        }
    }

}
