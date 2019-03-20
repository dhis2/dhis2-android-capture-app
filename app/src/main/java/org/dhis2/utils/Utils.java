package org.dhis2.utils;

import android.content.Context;
import android.view.View;
import android.widget.PopupMenu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 22/11/2018.
 */
public class Utils {

    public static PopupMenu getPopUpMenu(Context context, View anchor, int gravity, int menu, PopupMenu.OnMenuItemClickListener listener, boolean showIcons) {
        PopupMenu popupMenu = new PopupMenu(context, anchor, gravity);
        if (showIcons)
            try {
                Field[] fields = popupMenu.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popupMenu);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(listener);

        return popupMenu;
    }
}
