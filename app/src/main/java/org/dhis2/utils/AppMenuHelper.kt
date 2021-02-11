package org.dhis2.utils

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import org.dhis2.R
import timber.log.Timber

class AppMenuHelper private constructor(
    private val context: Context,
    private val menu: Int,
    private val anchor: View,
    private val onMenuInflated: (PopupMenu) -> Unit,
    private val onMenuItemClicked: (Int) -> Boolean
) {

    fun show() {
        val contextWrapper = ContextThemeWrapper(context, R.style.PopupMenuMarginStyle)
        val popupMenu = PopupMenu(contextWrapper, anchor, Gravity.END)
        try {
            val fields = popupMenu.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field[popupMenu]
                    val classPopupHelper =
                        Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod(
                        "setForceShowIcon",
                        Boolean::class.javaPrimitiveType
                    )
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        popupMenu.menuInflater.inflate(menu, popupMenu.menu)
        onMenuInflated(popupMenu)
        popupMenu.setOnMenuItemClickListener { onMenuItemClicked(it.itemId) }
        popupMenu.show()
    }

    data class Builder(
        var context: Context? = null,
        var menu: Int = -1,
        var anchor: View? = null,
        var onMenuInflated: (PopupMenu) -> Unit = {},
        var onMenuItemClicked: (Int) -> Boolean = { true }
    ) {

        fun menu(context: Context, @MenuRes menu: Int) = apply {
            this.menu = menu
            this.context = context
        }

        fun anchor(view: View) = apply {
            this.anchor = view
        }

        fun onMenuInflated(
            onMenuInflated: (PopupMenu) -> Unit
        ) = apply {
            this.onMenuInflated = onMenuInflated
        }

        fun onMenuItemClicked(
            onMenuItemClicked: (Int) -> Boolean
        ) = apply {
            this.onMenuItemClicked = onMenuItemClicked
        }

        fun build() = AppMenuHelper(
            context!!,
            menu,
            anchor!!,
            onMenuInflated,
            onMenuItemClicked
        )
    }
}
