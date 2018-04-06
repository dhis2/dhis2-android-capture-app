package com.dhis2.data.forms.dataentry.fields.radiobutton;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.common.ValueType;

/**
 * Created by frodriguez on 1/24/2018.
 */
@AutoValue
public abstract class RadioButtonViewModel extends FieldViewModel {

    public enum Value {
        CHECKED("true"), CHECKED_NO("false"), UNCHECKED("");

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
    public abstract Boolean mandatory();

    @Nullable
    public abstract Value value();

    @NonNull
    public abstract ValueType valueType();

    @NonNull
    public static RadioButtonViewModel fromRawValue(@NonNull String id, @NonNull String label, @NonNull ValueType type,
                                                    @NonNull Boolean mandatory, @Nullable String value) {
        if (value == null) {
            return new AutoValue_RadioButtonViewModel(id, label, mandatory, null, type);
        } else if (value.toLowerCase(Locale.US).equals(Value.CHECKED.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, mandatory, Value.CHECKED, type);
        } else if (value.toLowerCase(Locale.US).equals(Value.UNCHECKED.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, mandatory, Value.UNCHECKED, type);
        } else if (value.toLowerCase(Locale.US).equals(Value.CHECKED_NO.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, mandatory, Value.CHECKED_NO, type);
        } else {
            throw new IllegalArgumentException("Unsupported value: " + value);
        }
    }

}
