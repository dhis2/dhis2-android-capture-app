package com.data.utils;

import android.database.Cursor;
import android.support.annotation.NonNull;

public final class DbUtils {
    private DbUtils() {
        // no instances
    }

    @NonNull
    public static String string(@NonNull Cursor cursor, int column, @NonNull String fallback) {
        String value = cursor.getString(column);
        return value == null ? fallback : value;
    }

    @NonNull
    public static String escapeSqlToken(@NonNull String sqlString) {
        StringBuilder stringBuilder = new StringBuilder();
        if (sqlString.indexOf('\'') == -1) {
            return stringBuilder.append(sqlString).toString();
        }

        int length = sqlString.length();
        for (int i = 0; i < length; i++) {
            char c = sqlString.charAt(i);
            if (c == '\'') {
                stringBuilder.append('\'');
            }
            stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }
}
