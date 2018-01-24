package com.dhis2.data.forms.dataentry.fields.old_fields.edittext;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;

import com.google.auto.value.AutoValue;

import static org.hisp.dhis.android.dataentry.commons.utils.StringUtils.isEmpty;

@AutoValue
public abstract class EditTextDoubleViewModel extends EditTextModel<Double> {

    @NonNull
    public static EditTextDoubleViewModel fromRawValue(@NonNull String uid, @NonNull String label,
                                                       @NonNull Boolean mandatory, @Nullable String value, @NonNull String hint) {
        return create(uid, label, mandatory, isEmpty(value) ? null : Double.valueOf(value), hint);
    }

    @NonNull
    public static EditTextDoubleViewModel create(@NonNull String uid, @NonNull String label,
                                                 @NonNull Boolean mandatory, @Nullable Double value, @NonNull String hint) {
        return new AutoValue_EditTextDoubleViewModel(uid, label, mandatory,
                value, hint, 1, InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_NUMBER_FLAG_SIGNED, null, null);
    }

    @NonNull
    public EditTextDoubleViewModel withWarning(@NonNull String warning) {
        return new AutoValue_EditTextDoubleViewModel(uid(), label(), mandatory(),
                value(), hint(), maxLines(), inputType(), warning, error());
    }

    @NonNull
    public EditTextDoubleViewModel withError(@NonNull String error) {
        return new AutoValue_EditTextDoubleViewModel(uid(), label(), mandatory(),
                value(), hint(), maxLines(), inputType(), warning(), error);
    }
}
