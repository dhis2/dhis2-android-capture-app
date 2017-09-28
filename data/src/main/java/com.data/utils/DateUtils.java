package com.data.utils;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class DateUtils {

    private DateUtils() {
        // no instances
    }

    @NonNull
    public static SimpleDateFormat uiDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }

    @NonNull
    public static SimpleDateFormat databaseDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
    }

}