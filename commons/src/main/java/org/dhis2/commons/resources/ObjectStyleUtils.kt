package org.dhis2.commons.resources

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import org.dhis2.commons.R

object ObjectStyleUtils {
    @JvmStatic
    fun getIconResource(context: Context, resourceName: String?, defaultResource: Int): Drawable? {
        if (defaultResource == -1) {
            return null
        }
        val defaultDrawable = AppCompatResources.getDrawable(context, defaultResource)
        return if (resourceName?.isNotEmpty() == true) {
            val iconResource = ResourceManager(context)
                .getObjectStyleDrawableResource(resourceName, R.drawable.ic_default_icon)
            val drawable = AppCompatResources.getDrawable(context, iconResource)
            drawable?.mutate()
            drawable ?: defaultDrawable
        } else {
            defaultDrawable
        }
    }

    fun getColorResource(
        context: Context,
        styleColor: String?,
        @ColorRes defaultColorResource: Int
    ): Int {
        return styleColor?.let {
            val color: String = when {
                styleColor.startsWith("#") -> styleColor
                else -> "#$styleColor"
            }
            if (color.length == 4) {
                ContextCompat.getColor(context, defaultColorResource)
            } else {
                Color.parseColor(color)
            }
        } ?: ContextCompat.getColor(context, defaultColorResource)
    }
}
