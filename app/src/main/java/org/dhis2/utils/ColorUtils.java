package org.dhis2.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.R;

import java.util.ArrayList;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 12/06/2018.
 */

public class ColorUtils {

    public static int parseColor(@NonNull String hexColor) {
        if (hexColor.length() == 4) {//Color is formatted as #fff
            char r = hexColor.charAt(1);
            char g = hexColor.charAt(2);
            char b = hexColor.charAt(3);
            hexColor = "#" + r + r + g + g + b + b; //formatted to #ffff
        }
        return Color.parseColor(hexColor);
    }

    public static int getPrimaryColorWithAlpha(Context context, ColorType primaryLight, float alpha) {
        int primayColor = getPrimaryColor(context, primaryLight);
        return androidx.core.graphics.ColorUtils.setAlphaComponent(primayColor, 155);
    }

    public static int withAlpha(int color) {
        return androidx.core.graphics.ColorUtils.setAlphaComponent(color, 155);
    }

    public enum ColorType {
        PRIMARY, PRIMARY_LIGHT, PRIMARY_DARK, ACCENT
    }

    public static int getColorFrom(@Nullable String hexColor, int defaultPrimaryColor) {

        int colorToReturn = Color.BLACK;

        if (!isEmpty(hexColor)) {
            colorToReturn = parseColor(hexColor);
        }
        if (isEmpty(hexColor) || colorToReturn == Color.BLACK || colorToReturn == Color.WHITE) {
            colorToReturn = defaultPrimaryColor;
        }

        return colorToReturn;
    }

    public static Drawable tintDrawableReosurce(@NonNull Drawable drawableToTint, int bgResource) {
        drawableToTint.setColorFilter(getContrastColor(bgResource), PorterDuff.Mode.SRC_IN);
        return drawableToTint;
    }

    public static Drawable tintDrawableWithColor(@NonNull Drawable drawableToTint, int tintColor) {
        drawableToTint.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
        return drawableToTint;
    }

    public static int getContrastColor(int color) {

        ArrayList<Double> rgb = new ArrayList<>();
        rgb.add(Color.red(color) / 255.0d);
        rgb.add(Color.green(color) / 255.0d);
        rgb.add(Color.blue(color) / 255.0d);

        Double r = null;
        Double g = null;
        Double b = null;
        for (Double c : rgb) {
            if (c <= 0.03928d)
                c = c / 12.92d;
            else
                c = Math.pow(((c + 0.055d) / 1.055d), 2.4d);

            if (r == null)
                r = c;
            else if (g == null)
                g = c;
            else
                b = c;
        }

        double L = 0.2126d * r + 0.7152d * g + 0.0722d * b;


        return (L > 0.179d) ? Color.BLACK : Color.WHITE;
    }

    public static int getThemeFromColor(String color) {

        if (color == null)
            return -1;

        switch (color) {
            case "#ffcdd2":
                return R.style.colorPrimary_Pink;
            case "#e57373":
                return R.style.colorPrimary_e57;
            case "#d32f2f":
                return R.style.colorPrimary_d32;
            case "#f06292":
                return R.style.colorPrimary_f06;
            case "#c2185b":
                return R.style.colorPrimary_c21;
            case "#880e4f":
                return R.style.colorPrimary_880;
            case "#f50057":
                return R.style.colorPrimary_f50;
            case "#e1bee7":
                return R.style.colorPrimary_e1b;
            case "#ba68c8":
                return R.style.colorPrimary_ba6;
            case "#8e24aa":
                return R.style.colorPrimary_8e2;
            case "#aa00ff":
                return R.style.colorPrimary_aa0;
            case "#7e57c2":
                return R.style.colorPrimary_7e5;
            case "#4527a0":
                return R.style.colorPrimary_452;
            case "#7c4dff":
                return R.style.colorPrimary_7c4;
            case "#6200ea":
                return R.style.colorPrimary_620;
            case "#c5cae9":
                return R.style.colorPrimary_c5c;
            case "#7986cb":
                return R.style.colorPrimary_798;
            case "#3949ab":
                return R.style.colorPrimary_394;
            case "#304ffe":
                return R.style.colorPrimary_304;
            case "#e3f2fd":
                return R.style.colorPrimary_e3f;
            case "#64b5f6":
                return R.style.colorPrimary_64b;
            case "#1976d2":
                return R.style.colorPrimary_197;
            case "#0288d1":
                return R.style.colorPrimary_028;
            case "#40c4ff":
                return R.style.colorPrimary_40c;
            case "#00b0ff":
                return R.style.colorPrimary_00b;
            case "#80deea":
                return R.style.colorPrimary_80d;
            case "#00acc1":
                return R.style.colorPrimary_00a;
            case "#00838f":
                return R.style.colorPrimary_008;
            case "#006064":
                return R.style.colorPrimary_006;
            case "#e0f2f1":
                return R.style.colorPrimary_e0f;
            case "#80cbc4":
                return R.style.colorPrimary_80c;
            case "#00695c":
                return R.style.colorPrimary_0069;
            case "#64ffda":
                return R.style.colorPrimary_64f;
            case "#c8e6c9":
                return R.style.colorPrimary_c8e;
            case "#66bb6a":
                return R.style.colorPrimary_66b;
            case "#2e7d32":
                return R.style.colorPrimary_2e7;
            case "#60ad5e":
                return R.style.colorPrimary_60a;
            case "#00e676":
                return R.style.colorPrimary_00e;
            case "#aed581":
                return R.style.colorPrimary_aed;
            case "#689f38":
                return R.style.colorPrimary_689;
            case "#33691e":
                return R.style.colorPrimary_336;
            case "#76ff03":
                return R.style.colorPrimary_76f;
            case "#64dd17":
                return R.style.colorPrimary_64d;
            case "#cddc39":
                return R.style.colorPrimary_cdd;
            case "#9e9d24":
                return R.style.colorPrimary_9e9;
            case "#827717":
                return R.style.colorPrimary_827;
            case "#fff9c4":
                return R.style.colorPrimary_fff;
            case "#fbc02d":
                return R.style.colorPrimary_fbc;
            case "#f57f17":
                return R.style.colorPrimary_f57;
            case "#ffff00":
                return R.style.colorPrimary_ffff;
            case "#ffcc80":
                return R.style.colorPrimary_ffc;
            case "#ffccbc":
                return R.style.colorPrimary_ffcc;
            case "#ffab91":
                return R.style.colorPrimary_ffa;
            case "#bcaaa4":
                return R.style.colorPrimary_bca;
            case "#8d6e63":
                return R.style.colorPrimary_8d6;
            case "#4e342e":
                return R.style.colorPrimary_4e3;
            case "#fafafa":
                return R.style.colorPrimary_faf;
            case "#bdbdbd":
                return R.style.colorPrimary_bdb;
            case "#757575":
                return R.style.colorPrimary_757;
            case "#424242":
                return R.style.colorPrimary_424;
            case "#cfd8dc":
                return R.style.colorPrimary_cfd;
            case "#b0bec5":
                return R.style.colorPrimary_b0b;
            case "#607d8b":
                return R.style.colorPrimary_607;
            case "#37474f":
                return R.style.colorPrimary_374;
            default:
                return -1;

        }
    }

    public static int getPrimaryColor(Context context, @NonNull ColorType colorType) {

        int id;
        switch (colorType) {
            case ACCENT:
                id = R.attr.colorAccent;
                break;
            case PRIMARY_DARK:
                id = R.attr.colorPrimaryDark;
                break;
            case PRIMARY_LIGHT:
                id = R.attr.colorPrimaryLight;
                break;
            case PRIMARY:
            default:
                id = R.attr.colorPrimary;
                break;
        }

        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{id});
        int colorToReturn = a.getColor(0, 0);
        a.recycle();
        return colorToReturn;
    }
}