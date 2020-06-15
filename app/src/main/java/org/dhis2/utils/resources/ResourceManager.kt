package org.dhis2.utils.resources

import android.content.Context
import androidx.annotation.DrawableRes

class ResourceManager(val context: Context) {

    fun getObjectStyleDrawableResource(icon: String?, @DrawableRes defaultResource: Int): Int {
        return icon?.let {
            val iconName = if (icon.startsWith("ic_")) icon else "ic_$icon"
            context.resources.getIdentifier(iconName, "drawable", context.packageName)
        } ?: defaultResource
    }
}
