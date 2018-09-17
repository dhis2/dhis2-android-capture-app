package org.dhis2.data.forms.dataentry.fields;

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

    @Nullable
    public abstract Boolean allowFutureDate();

    @Nullable
    public abstract Boolean editable();

    @Nullable
    public abstract String optionSet();

    @Nullable
    public abstract String warning();

    @Nullable
    public abstract String error();

    public abstract FieldViewModel setMandatory();

    @NonNull
    public abstract FieldViewModel withWarning(@NonNull String warning);

    @NonNull
    public abstract FieldViewModel withError(@NonNull String error);
}