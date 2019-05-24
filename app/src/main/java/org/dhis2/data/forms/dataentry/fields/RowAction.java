package org.dhis2.data.forms.dataentry.fields;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RowAction {

    @NonNull
    public abstract String id();

    @Nullable
    public abstract String value();

    @NonNull
    public abstract Boolean requiresExactMatch();

    @Nullable
    public abstract String optionCode();

    @Nullable
    public abstract String optionName();

    public abstract Integer lastFocusPosition();

    @NonNull
    public static RowAction create(@NonNull String id, @Nullable String value) {
        return new AutoValue_RowAction(id, value, false, null, null, -1);
    }

    @NonNull
    public static RowAction create(@NonNull String id, @Nullable String value, int lastFocusPosition) {
        return new AutoValue_RowAction(id, value, false, null, null, lastFocusPosition);
    }

    @NonNull
    public static RowAction create(@NonNull String id, @Nullable String value, @NonNull Boolean requieresExactMatch) {
        return new AutoValue_RowAction(id, value, requieresExactMatch, null, null, -1);
    }

    @NonNull
    public static RowAction create(@NonNull String id, @Nullable String value, @NonNull Boolean requieresExactMatch, @NonNull String code, @NonNull String name, int lastAdapterPosition) {
        return new AutoValue_RowAction(id, value, requieresExactMatch, code, name, lastAdapterPosition);
    }
}
