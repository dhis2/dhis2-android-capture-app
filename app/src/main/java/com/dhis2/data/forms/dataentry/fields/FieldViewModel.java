package com.dhis2.data.forms.dataentry.fields;

import android.support.annotation.NonNull;

public abstract class FieldViewModel {

    @NonNull
    public abstract String uid();

    @NonNull
    public abstract String label();
}