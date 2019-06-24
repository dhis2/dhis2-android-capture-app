package org.dhis2.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable

import androidx.appcompat.content.res.AppCompatResources

object ObjectStyleUtils {

    fun getIconResource(context: Context, resourceName: String?, defaultResource: Int): Drawable? {
        val defaultDrawable = AppCompatResources.getDrawable(context, defaultResource)

        return if (resourceName != null) {
            val resources = context.resources
            val iconName = if (resourceName.startsWith("ic_")) resourceName else "ic_$resourceName"
            val iconResource = resources.getIdentifier(iconName, "drawable", context.packageName)

            val drawable = AppCompatResources.getDrawable(context, iconResource)

            drawable ?: defaultDrawable
        } else
            defaultDrawable
    }

}
