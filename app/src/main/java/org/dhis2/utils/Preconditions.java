package org.dhis2.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Preconditions {
    private Preconditions() {
        // no instances
    }

    @NonNull
    public static <T> T isNull(@Nullable T obj, @NonNull String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }

        return obj;
    }

    public static boolean equals(@Nullable Object one, @Nullable Object two) {
        return one == two || one != null && one.equals(two); // NOPMD
    }
}