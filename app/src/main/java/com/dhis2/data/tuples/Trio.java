package com.dhis2.data.tuples;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Trio<A, B, C> {

    @NonNull
    public abstract A val0();

    @NonNull
    public abstract B val1();

    @NonNull
    public abstract C val2();

    @NonNull
    public static <A, B, C> Trio<A, B, C> create(@NonNull A val0, @NonNull B val1, @NonNull C val2) {
        return new AutoValue_Trio<>(val0, val1, val2);
    }
}
