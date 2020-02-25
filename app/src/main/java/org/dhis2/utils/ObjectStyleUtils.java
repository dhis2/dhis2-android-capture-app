package org.dhis2.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import static android.text.TextUtils.isEmpty;

public class ObjectStyleUtils {

    public static Drawable getIconResource(Context context, String resourceName, int defaultResource) {
        if (defaultResource == -1) {
            return null;
        }
        Drawable defaultDrawable = AppCompatResources.getDrawable(context, defaultResource);

        if (!isEmpty(resourceName)) {
            Resources resources = context.getResources();
            String iconName = resourceName.startsWith("ic_") ? resourceName : "ic_" + resourceName;
            int iconResource = resources.getIdentifier(iconName, "drawable", context.getPackageName());

            Drawable drawable = AppCompatResources.getDrawable(context, iconResource);

            if (drawable != null)
                drawable.mutate();

            return drawable != null ? drawable : defaultDrawable;
        } else
            return defaultDrawable;
    }

    public static int getColorResource(Context context, String styleColor, @ColorRes int defaultColorResource) {
        if (styleColor == null) {
            return ContextCompat.getColor(context, defaultColorResource);
        } else {
            String color = styleColor.startsWith("#") ? styleColor : "#" + styleColor;
            int colorRes;
            if (color.length() == 4)
                return ContextCompat.getColor(context, defaultColorResource);
            else
                colorRes = Color.parseColor(color);

            return colorRes;
        }
    }
}
