package org.dhis2.commons.data.tuples;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Quartet<A, B, C, D> {

    @NonNull
    public abstract A val0();

    @NonNull
    public abstract B val1();

    @NonNull
    public abstract C val2();

    @NonNull
    public abstract D val3();

    @NonNull
    public static <A, B, C, D> Quartet<A, B, C, D> create(@NonNull A val0, @NonNull B val1,
                                                          @NonNull C val2, @NonNull D val3) {
        return new AutoValue_Quartet<>(val0, val1, val2, val3);
    }
}
