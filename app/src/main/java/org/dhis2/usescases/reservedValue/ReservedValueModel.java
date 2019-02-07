package org.dhis2.usescases.reservedValue;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class ReservedValueModel {

    @NonNull
    public abstract String uid();

    @Nullable
    public abstract String orgUnitUid();

    @NonNull
    public abstract Boolean patternContainsOU();

    @Nullable
    public abstract String orgUnitName();

    @NonNull
    public abstract String displayName();

    @NonNull
    public abstract Integer reservedValues();

    @NonNull
    public static ReservedValueModel create(@NonNull String uid,
                                            @NonNull String displayName,
                                            boolean patternContainsOU,
                                            @Nullable String orgUnitUid, @Nullable String orgUnitName,
                                            int reservedValue) {
        return new AutoValue_ReservedValueModel(uid, orgUnitUid, patternContainsOU, orgUnitName, displayName, reservedValue);
    }

}
