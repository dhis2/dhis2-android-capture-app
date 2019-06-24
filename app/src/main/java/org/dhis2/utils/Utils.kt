package org.dhis2.utils

import android.content.Context
import android.view.View
import android.widget.PopupMenu

import java.lang.reflect.Field
import java.lang.reflect.Method

import timber.log.Timber

/**
 * QUADRAM. Created by ppajuelo on 22/11/2018.
 */
object Utils {

    fun getPopUpMenu(context: Context, anchor: View, gravity: Int, menu: Int, listener: PopupMenu.OnMenuItemClickListener, showIcons: Boolean): PopupMenu {
        val popupMenu = PopupMenu(context, anchor, gravity)
        if (showIcons)
            try {
                val fields = popupMenu.javaClass.declaredFields
                for (field in fields) {
                    if ("mPopup" == field.name) {
                        field.isAccessible = true
                        val menuPopupHelper = field.get(popupMenu)
                        val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                        val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType!!)
                        setForceIcons.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }

        popupMenu.menuInflater.inflate(menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(listener)

        return popupMenu
    }
}
