package org.dhis2.commons.data.tuples;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Quintet<A, B, C, D, E> {

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
    public static <A, B, C, D, E> Quintet<A, B, C, D, E> create(@NonNull A val0,
                                                                @NonNull B val1, @NonNull C val2,
                                                                @NonNull D val3, @NonNull E val4) {
        return new AutoValue_Quintet<>(val0, val1, val2, val3, val4);
    }
}
