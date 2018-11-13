package org.dhis2.usescases.reservedValue;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ReservedValueModel {

    @NonNull
    public abstract String uid();

    @NonNull
    public abstract String orgUnitUid();

    @NonNull
    public abstract String orgUnitName();

    @NonNull
    public abstract String displayName();

    @NonNull
    public abstract String reservedValue();

    @NonNull
    public static ReservedValueModel create(@NonNull String uid, @NonNull String orgUnitUid, @NonNull String orgUnitName, @NonNull String displayName, @NonNull String reservedValue) {
        return new AutoValue_ReservedValueModel(uid, orgUnitUid, orgUnitName, displayName, reservedValue);
    }

}
