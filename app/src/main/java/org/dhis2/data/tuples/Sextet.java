package org.dhis2.data.tuples;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Sextet<A, B, C, D, E, F> {

    @NonNull
    public abstract A val0();

    @NonNull
    public abstract B val1();

    @NonNull
    public abstract C val2();

    @NonNull
    public abstract D val3();

    @NonNull
    public abstract E val4();

    @NonNull
    public abstract F val5();

    @NonNull
    public static <A, B, C, D, E, F> Sextet<A, B, C, D, E, F> create(@NonNull A val0,
                                                                     @NonNull B val1, @NonNull C val2,
                                                                     @NonNull D val3, @NonNull E val4, @NonNull F val5) {
        return new AutoValue_Sextet<>(val0, val1, val2, val3, val4, val5);
    }
}
