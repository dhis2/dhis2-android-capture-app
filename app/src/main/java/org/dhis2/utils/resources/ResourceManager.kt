package org.dhis2.utils.resources

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import org.dhis2.R

class ResourceManager(val context: Context) {

    fun getObjectStyleDrawableResource(icon: String?, @DrawableRes defaultResource: Int): Int {
        return icon?.let {
            val iconName = if (icon.startsWith("ic_")) icon else "ic_$icon"
            var iconResource =
                context.resources.getIdentifier(iconName, "drawable", context.packageName)
            if (iconResource != 0 && iconResource != -1 && drawableExists(iconResource)
            ) {
                iconResource
            } else {
                R.drawable.ic_default_icon
            }
        } ?: defaultResource
    }

    private fun drawableExists(iconResource: Int): Boolean {
        return try {
            ContextCompat.getDrawable(context, iconResource)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun defaultEventLabel(): String = context.getString(R.string.events)
    fun defaultDataSetLabel(): String = context.getString(R.string.data_sets)
    fun defaultTeiLabel(): String = context.getString(R.string.tei)
}
