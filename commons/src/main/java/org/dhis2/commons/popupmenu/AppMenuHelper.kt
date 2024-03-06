package org.dhis2.commons.popupmenu

import android.content.Context
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import org.dhis2.commons.R

class AppMenuHelper private constructor(
    private val context: Context,
    private val menu: Int,
    private val anchor: View,
    private val onMenuInflated: (PopupMenu) -> Unit,
    private val onMenuItemClicked: (Int) -> Boolean,
    private val onException: ((Exception) -> Unit)? = {}
) {

    lateinit var popupMenu: PopupMenu

    fun show() {
        val contextWrapper = ContextThemeWrapper(context, R.style.PopupMenuMarginStyle)
        popupMenu = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            PopupMenu(contextWrapper, anchor, Gravity.END, 0, R.style.PopupMenuMarginStyle)
        } else {
            PopupMenu(contextWrapper, anchor, Gravity.END)
        }
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
            onException?.invoke(e)
        }
        popupMenu.menuInflater.inflate(menu, popupMenu.menu)
        MenuCompat.setGroupDividerEnabled(popupMenu.menu, true)
        onMenuInflated(popupMenu)
        popupMenu.setOnMenuItemClickListener { onMenuItemClicked(it.itemId) }
        popupMenu.show()
    }

    fun addIconToItem(@IdRes id: Int, @DrawableRes icon: Int) {
        popupMenu.menu.findItem(id)?.icon = ContextCompat.getDrawable(this.context, icon)
    }

    fun addIconToItemInvisible(@IdRes id: Int, @DrawableRes icon: Int) {
        popupMenu.menu.findItem(id)?.icon = ContextCompat.getDrawable(this.context, icon)
        popupMenu.menu.findItem(id)?.icon?.alpha = 0
    }

    fun changeItemText(@IdRes id: Int, text: String) {
        popupMenu.menu.findItem(id)?.title = text
    }

    fun getItemText(@IdRes id: Int): String {
        return popupMenu.menu.findItem(id)?.title.toString()
    }

    fun hideItem(@IdRes id: Int) {
        popupMenu.menu.findItem(id)?.isVisible = false
    }

    fun showItem(@IdRes id: Int) {
        popupMenu.menu.findItem(id)?.isVisible = true
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

        fun onMenuInflated(onMenuInflated: (PopupMenu) -> Unit) = apply {
            this.onMenuInflated = onMenuInflated
        }

        fun onMenuItemClicked(onMenuItemClicked: (Int) -> Boolean) = apply {
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
