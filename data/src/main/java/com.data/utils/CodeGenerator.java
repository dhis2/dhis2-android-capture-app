package com.data.utils;

import android.support.annotation.NonNull;

public interface CodeGenerator {

    /**
     * Generates a pseudo random string using the allowed characters. Code is
     * 11 characters long.
     */
    @NonNull
    String generate();
}
