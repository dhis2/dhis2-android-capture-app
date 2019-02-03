package org.dhis2.data.forms.dataentry.tablefields;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.List;


@AutoValue
public abstract class RowAction {

    @NonNull
    public abstract String id();

    @Nullable
    public abstract String value();

    @Nullable
    public abstract String dataElement();

    @Nullable
    public abstract List<String> listCategoryOption();

    @NonNull
    public static RowAction create(@NonNull String id, @Nullable String value, @Nullable String dataElement, @Nullable List<String> listCategoryOption) {
        return new AutoValue_RowAction(id, value, dataElement, listCategoryOption);
    }
}
