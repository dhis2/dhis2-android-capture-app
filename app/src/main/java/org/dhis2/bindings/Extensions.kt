package org.dhis2.bindings

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import org.dhis2.App
import org.dhis2.data.user.UserComponent
import java.text.DecimalFormat

fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = this.apply { setValue(initialValue) }

fun Fragment.app(): App {
    return context?.applicationContext as App
}

fun Fragment.userComponent(): UserComponent? {
    return app().userComponent()
}

fun AppCompatActivity.app(): App {
    return applicationContext as App
}

fun AppCompatActivity.userComponent(): UserComponent? {
    return app().userComponent()
}

fun Context.app(): App {
    return applicationContext as App
}

fun Context.drawableFrom(@DrawableRes drawableResource: Int): Drawable? {
    return AppCompatResources.getDrawable(this, drawableResource)
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Double.decimalFormat: String
    get() = DecimalFormat("0.##############").format(this)

fun AppCompatActivity.isKeyboardOpened(): Boolean {
    val r = Rect()
    val keyboardVisibilityThreshold = 100

    val activityRoot = (findViewById<ViewGroup>(android.R.id.content)).getChildAt(0)
    val visibleThreshold = keyboardVisibilityThreshold.dp

    if (activityRoot == null) return false
    activityRoot.getWindowVisibleDisplayFrame(r)

    val heightDiff = activityRoot.rootView.height - r.height()

    return heightDiff > visibleThreshold
}
