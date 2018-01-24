package com.dhis2.data.forms.dataentry.fields.old_fields.edittext;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.dataentry.form.dataentry.fields.EditableFieldViewModel;

public abstract class EditTextModel<T> extends EditableFieldViewModel<T> {

    @NonNull
    public abstract String hint();

    @NonNull
    public abstract Integer maxLines();

    @NonNull
    public abstract Integer inputType();

    @Nullable
    public abstract String warning();

    @Nullable
    public abstract String error();
}