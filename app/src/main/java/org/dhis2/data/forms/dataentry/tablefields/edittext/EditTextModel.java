package org.dhis2.data.forms.dataentry.tablefields.edittext;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dhis2.data.forms.dataentry.tablefields.EditableFieldViewModel;
import org.hisp.dhis.android.core.common.ValueType;


public abstract class EditTextModel<T> extends EditableFieldViewModel<T> {

    @NonNull
    public abstract String hint();

    @NonNull
    public abstract Integer maxLines();

    @NonNull
    public abstract Integer inputType();

    @NonNull
    public abstract ValueType valueType();

    @Nullable
    public abstract String warning();

    @Nullable
    public abstract String error();
}