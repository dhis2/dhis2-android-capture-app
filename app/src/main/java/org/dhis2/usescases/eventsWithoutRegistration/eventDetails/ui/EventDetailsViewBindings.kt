package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import org.dhis2.R
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.common.ObjectStyle

@BindingAdapter("set_icon_style")
fun ImageView.setIconStyle(style: ObjectStyle?) {
    style?.let {

        val color = ColorUtils.getColorFrom(
            style.color(),
            ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY_LIGHT)
        )

        background = ColorUtils.tintDrawableWithColor(
            background,
            color
        )

        setImageResource(
            ResourceManager(context).getObjectStyleDrawableResource(
                style.icon(),
                R.drawable.ic_program_default
            )
        )

        setColorFilter(ColorUtils.getContrastColor(color))
    }
}
