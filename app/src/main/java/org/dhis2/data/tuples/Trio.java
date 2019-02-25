package org.dhis2.data.tuples;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Trio<A, B, C> {

    @NonNull
    public abstract A val0();

    @NonNull
    public abstract B val1();

    @Nullable
    public abstract C val2();

    @NonNull
    public static <A, B, C> Trio<A, B, C> create(@Nullable A val0, @Nullable B val1, @Nullable C val2) {
        return new AutoValue_Trio<>(val0, val1, val2);
    }
}
