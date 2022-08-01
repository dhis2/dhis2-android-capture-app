package org.dhis2.commons.data.tuples;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Pair<A, B> {

    @NonNull
    public abstract A val0();

    @NonNull
    public abstract B val1();

    @NonNull
    public static <A, B> Pair<A, B> create(@NonNull A val0, @NonNull B val1) {
        return new AutoValue_Pair<>(val0, val1);
    }
}
