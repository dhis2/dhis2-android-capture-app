package com.dhis2.data.forms.dataentry.fields;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class FieldViewModel {

    @NonNull
    public abstract String uid();

    @NonNull
    public abstract String label();

    @NonNull
    public abstract Boolean mandatory();

    @Nullable
    public abstract String value();

    @Nullable
    public abstract String programStageSection();
}