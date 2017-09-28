package com.data.utils;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;

import java.util.Iterator;
import java.util.List;

public final class StringUtils {
    private StringUtils() {
        // no instances
    }

    public static boolean isEmpty(@Nullable CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    @NonNull
    public static String join(@NonNull List<String> values) {
        StringBuilder cache = new StringBuilder();

        Iterator<String> labelIterator = values.iterator();
        while (labelIterator.hasNext()) {
            cache.append(labelIterator.next());

            // new line after each label
            if (labelIterator.hasNext()) {
                cache.append('\n');
            }
        }

        return cache.toString();
    }

    @NonNull
    public static Spanned htmlify(@NonNull List<String> values) {
        StringBuilder cache = new StringBuilder();

        Iterator<String> labelIterator = values.iterator();
        while (labelIterator.hasNext()) {
            cache.append(labelIterator.next());

            // new line after each label
            if (labelIterator.hasNext()) {
                cache.append("<br/>");
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(cache.toString(), Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(cache.toString());
        }
    }
}
