package org.dhis2.data.forms.dataentry.fields.radiobutton;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.ValueType;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */
@AutoValue
public abstract class RadioButtonViewModel extends FieldViewModel {

    public enum Value {
        CHECKED("true"), CHECKED_NO("false"), UNCHECKED("");

        @NonNull
        private final String mValue;

        Value(@NonNull String value) {
            this.mValue = value;
        }

        @NonNull
        @Override
        public String toString() {
            return mValue;
        }
    }

    @NonNull
    public abstract Boolean mandatory();


    @NonNull
    public abstract ValueType valueType();

    @NonNull
    @SuppressWarnings("squid:S00107")
    public static RadioButtonViewModel fromRawValue(@NonNull String id, @NonNull String label, @NonNull ValueType type,
                                                    @NonNull Boolean mandatory, @Nullable String value, @Nullable String section,
                                                    Boolean editable, @Nullable String description, ObjectStyleModel objectStyle) {
        if (value == null) {
            return new AutoValue_RadioButtonViewModel(id, label, null, section, null, editable, null, null, null, description, objectStyle, mandatory, type);
        } else if (value.equalsIgnoreCase(Value.CHECKED.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.CHECKED.toString(), section, null, editable, null, null, null, description, objectStyle, mandatory, type);
        } else if (value.equalsIgnoreCase(Value.UNCHECKED.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.UNCHECKED.toString(), section, null, editable, null, null, null, description, objectStyle, mandatory, type);
        } else if (value.equalsIgnoreCase(Value.CHECKED_NO.toString())) {
            return new AutoValue_RadioButtonViewModel(id, label, Value.CHECKED_NO.toString(), section, null, editable, null, null, null, description, objectStyle, mandatory, type);
        } else {
            throw new IllegalArgumentException("Unsupported VALUE: " + value);
        }
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), true, valueType());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), mandatory(), valueType());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), value(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), mandatory(), valueType());
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_RadioButtonViewModel(uid(), label(), data, programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), mandatory(), valueType());
    }
}
