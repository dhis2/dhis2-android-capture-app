package org.dhis2.data.forms.dataentry.tablefields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @Nullable
    public abstract String catCombo();

    public abstract int rowPos();


    public abstract int columnPos();

    public abstract String catOptCombo();

    @NonNull
    public static RowAction create(@NonNull String id, @Nullable String value, @Nullable String dataElement, @Nullable String catOptComb, @Nullable String catCombo,
                                   int rowPos, int columnPos) {
        return new AutoValue_RowAction(id, value, dataElement, null, catCombo, rowPos, columnPos, catOptComb);
    }
}
