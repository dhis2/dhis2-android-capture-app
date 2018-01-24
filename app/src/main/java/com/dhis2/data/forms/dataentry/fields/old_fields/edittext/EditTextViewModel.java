package com.dhis2.data.forms.dataentry.fields.old_fields.edittext;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EditTextViewModel extends EditTextModel<String> {

    @NonNull
    public static EditTextViewModel create(@NonNull String uid, @NonNull String label,
                                           @NonNull Boolean mandatory, @Nullable String value, @NonNull String hint,
                                           @NonNull Integer lines) {
        return new AutoValue_EditTextViewModel(uid, label, mandatory,
                value, hint, lines, InputType.TYPE_CLASS_TEXT, null, null);
    }

    @NonNull
    public EditTextViewModel withWarning(@NonNull String warning) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), hint(), maxLines(), inputType(), warning, error());
    }

    @NonNull
    public EditTextViewModel withError(@NonNull String error) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), hint(), maxLines(), inputType(), warning(), error);
    }
}
