package org.dhis2.utils.resources

import android.content.Context
import androidx.annotation.DrawableRes
import org.dhis2.R

class ResourceManager(val context: Context) {

    fun getObjectStyleDrawableResource(icon: String?, @DrawableRes defaultResource: Int): Int {
        return icon?.let {
            val iconName = if (icon.startsWith("ic_")) icon else "ic_$icon"
            var iconResource =
                context.resources.getIdentifier(iconName, "drawable", context.packageName)
            if (iconResource != 0 && iconResource != -1) {
                iconResource
            } else {
                R.drawable.ic_default_icon
            }
        } ?: defaultResource
    }
}
