package com.dhis2.data.forms.dataentry.fields.edittext;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.common.ValueType;

/**
 * Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class EditTextViewModel extends EditTextModel<String> {

    @NonNull
    public static EditTextViewModel create(@NonNull String uid, @NonNull String label,
                                           @NonNull Boolean mandatory, @Nullable String value, @NonNull String hint,
                                           @NonNull Integer lines, @NonNull ValueType valueType) {
        return new AutoValue_EditTextViewModel(uid, label, mandatory,
                value, hint, lines, InputType.TYPE_CLASS_TEXT, valueType, null, null);
    }

    @NonNull
    public EditTextViewModel withWarning(@NonNull String warning) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), hint(), maxLines(), inputType(),valueType(), warning, error());
    }

    @NonNull
    public EditTextViewModel withError(@NonNull String error) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), hint(), maxLines(), inputType(),valueType(), warning(), error);
    }

}
