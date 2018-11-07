package org.dhis2.data.forms.dataentry.tablefields;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;


@AutoValue
public abstract class RowAction {

    @NonNull
    public abstract String id();

    @Nullable
    public abstract String value();

    @NonNull
    public static RowAction create(@NonNull String id, @Nullable String value) {
        return new AutoValue_RowAction(id, value);
    }
}
