package org.dhis2.bindings

import android.graphics.Outline
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ListPopupWindow
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tbuonomo.viewpagerdotsindicator.R
import org.dhis2.commons.extensions.closeKeyboard
import java.lang.Exception

fun View.getThemePrimaryColor(): Int {
    val value = TypedValue()
    context.theme.resolveAttribute(R.attr.colorPrimary, value, true)
    return value.data
}

fun View.onFocusRemoved(onFocusRemovedCallback: () -> Unit) {
    setOnFocusChangeListener { view, hasFocus ->
        if (!hasFocus) {
            closeKeyboard()
            onFocusRemovedCallback.invoke()
        }
    }
}

fun TextView.clearFocusOnDone() {
    setOnEditorActionListener { view, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            view.clearFocus()
            true
        } else {
            false
        }
    }
}

fun View.clipWithRoundedCorners(curvedRadio: Int = 16.dp) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    view.height + curvedRadio,
                    curvedRadio.toFloat(),
                )
            }
        }
        clipToOutline = true
    }
}

fun View.clipWithAllRoundedCorners(curvedRadio: Int = 16.dp) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    view.height,
                    curvedRadio.toFloat(),
                )
            }
        }
        clipToOutline = true
    }
}

fun Spinner.overrideHeight(desiredHeight: Int) {
    try {
        val popup = Spinner::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true

        // Get private mPopup member variable and try cast to ListPopupWindow
        val popupWindow = popup[this] as ListPopupWindow

        // Set popupWindow height
        popupWindow.height = desiredHeight
    } catch (e: Exception) {
        // silently fail...
    }
}

fun Spinner.doOnItemSelected(onItemSelected: (selectedIndex: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            adapterView: AdapterView<*>?,
            view: View?,
            selectedIndex: Int,
            id: Long,
        ) {
            onItemSelected(selectedIndex)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            // Don't do anything
        }
    }
}

fun FloatingActionButton.display(shouldBeDisplayed: Boolean) {
    if (shouldBeDisplayed) {
        show()
    } else {
        hide()
    }
}
