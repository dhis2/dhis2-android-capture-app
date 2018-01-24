package com.dhis2.data.forms.dataentry.fields.old_fields.edittext;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import timber.log.Timber;

import static org.hisp.dhis.android.dataentry.commons.utils.StringUtils.isEmpty;

@AutoValue
public abstract class EditTextIntegerViewModel extends EditTextModel<Integer> {

    @NonNull
    public static EditTextIntegerViewModel fromRawValue(@NonNull String uid, @NonNull String label,
                                                        @NonNull Boolean mandatory, @Nullable String value,
                                                        @NonNull String hint, @NonNull Integer type) {
        Integer intValue = null;
        if (!isEmpty(value)) {
            try {
                intValue = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                // the value in the database is invalid (probably too big)
                // return null value and let the user input a new value
                Timber.e(e, "Uid: " + uid + ". Label: " + label);
            }
        }
        return create(uid, label, mandatory, intValue, hint, type);
    }

    @NonNull
    public static EditTextIntegerViewModel create(@NonNull String uid, @NonNull String label,
                                                  @NonNull Boolean mandatory, @Nullable Integer value,
                                                  @NonNull String hint, @NonNull Integer type) {
        return new AutoValue_EditTextIntegerViewModel(uid, label, mandatory,
                value, hint, 1, type, null, null);
    }

    @NonNull
    public EditTextIntegerViewModel withWarning(@NonNull String warning) {
        return new AutoValue_EditTextIntegerViewModel(uid(), label(), mandatory(),
                value(), hint(), maxLines(), inputType(), warning, error());
    }

    @NonNull
    public EditTextIntegerViewModel withError(@NonNull String error) {
        return new AutoValue_EditTextIntegerViewModel(uid(), label(), mandatory(),
                value(), hint(), maxLines(), inputType(), warning(), error);
    }
}
