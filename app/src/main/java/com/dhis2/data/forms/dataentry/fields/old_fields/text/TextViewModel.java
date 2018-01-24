package com.dhis2.data.forms.dataentry.fields.old_fields.text;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.dataentry.form.dataentry.fields.FieldViewModel;

@AutoValue
public abstract class TextViewModel extends FieldViewModel {

    @NonNull
    public abstract String value();

    @NonNull
    public static TextViewModel create(@NonNull String uid,
                                       @NonNull String label, @NonNull String value) {
        return new AutoValue_TextViewModel(uid, label, value);
    }
}