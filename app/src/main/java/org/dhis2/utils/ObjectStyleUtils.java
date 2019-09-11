package org.dhis2.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

public class ObjectStyleUtils {

    public static Drawable getIconResource(Context context, String resourceName, int defaultResource) {
        Drawable defaultDrawable = AppCompatResources.getDrawable(context, defaultResource);

        if (resourceName != null) {
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

    public static int getIconResourceId(Context context, String resourceName, int defaultResource) {
        if (resourceName != null) {
            Resources resources = context.getResources();
            String iconName = resourceName.startsWith("ic_") ? resourceName : "ic_" + resourceName;
            return resources.getIdentifier(iconName, "drawable", context.getPackageName());
        } else
            return defaultResource;
    }

}
