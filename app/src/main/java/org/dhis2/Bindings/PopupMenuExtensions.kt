package org.dhis2.Bindings

import android.widget.PopupMenu
import timber.log.Timber

fun PopupMenu.showIcons() {
    try {
        val fields = this.javaClass.declaredFields
        for (field in fields) {
            if ("mPopup" == field.name) {
                field.isAccessible = true
                val menuPopupHelper = field.get(this)
                val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                val setForceIcons =
                    classPopupHelper.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
                setForceIcons.invoke(menuPopupHelper, true)
                break
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
}