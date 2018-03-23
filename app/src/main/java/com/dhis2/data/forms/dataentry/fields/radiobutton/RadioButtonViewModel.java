package com.dhis2.data.forms.dataentry.fields.radiobutton;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.forms.dataentry.fields.EditableFieldViewModel;
import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.common.ValueType;

import java.util.Locale;

/**
 * Created by frodriguez on 1/24/2018.
 */
@AutoValue
public abstract class RadioButtonViewModel extends FieldViewModel {

    @NonNull
    public abstract Boolean mandatory();

    @Nullable
    public abstract String value();

    @NonNull
    public abstract ValueType valueType();

    @NonNull
    public static RadioButtonViewModel fromRawValue(@NonNull String id, @NonNull String label, @NonNull ValueType type,
                                                    @NonNull Boolean mandatory, @Nullable String value) {
        return new AutoValue_RadioButtonViewModel(id, label, mandatory, value, type);
        /*if (value == null) {
            return new AutoValue_RadioButtonViewModel(uid, label, mandatory, null);
        } else if (value.toLowerCase(Locale.US).equals(Value.YES.toString())) {
            return new AutoValue_RadioButtonViewModel(uid, label, mandatory, null);
        } else if (value.toLowerCase(Locale.US).equals(Value.NO.toString())) {
            return new AutoValue_RadioButtonViewModel(uid, label, mandatory, null);
        } else if (value.isEmpty()) {
            return new AutoValue_RadioButtonViewModel(uid, label, mandatory, null);
        } else {
            throw new IllegalArgumentException("Unsupported value: " + value);
        }*/
    }

}
