package com.data.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public final class TypefaceManager {
    private static final String FONTS_PATH = "fonts/";

    private TypefaceManager() {
    }

    public static Typeface getTypeface(AssetManager assetManager, final String fontName) {
        if (assetManager == null) {
            throw new IllegalArgumentException("AssetManager must not be null");
        }

        if (fontName == null) {
            throw new IllegalArgumentException("AssetManager must not be null");
        }

        return Typeface.createFromAsset(assetManager, FONTS_PATH + fontName);
    }
}
