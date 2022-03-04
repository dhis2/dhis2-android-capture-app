package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import org.dhis2.R
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.databinding.CategorySelectorBinding
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
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
                R.drawable.ic_default_outline
            )
        )

        setColorFilter(ColorUtils.getContrastColor(color))
    }
}

@BindingAdapter(value = ["setViewModel", "setCatCombo"])
fun LinearLayout.setCatCombo(viewModel: EventDetailsViewModel, eventCatCombo: EventCatCombo) {
    if (!eventCatCombo.isDefault) {
        this@setCatCombo.removeAllViews()
        eventCatCombo.categories.forEach { category ->
            val catSelectorBinding: CategorySelectorBinding =
                CategorySelectorBinding.inflate(LayoutInflater.from(context))
            catSelectorBinding.catCombLayout.hint = category.name
            catSelectorBinding.catCombo.isEnabled = viewModel.eventDetails.value?.enabled ?: true
            catSelectorBinding.catCombo.setOnClickListener {
                viewModel.onCatComboClick(category)
            }

            val selectorDisplay =
                eventCatCombo.selectedCategoryOptions[category.uid]?.displayName()
                    ?: eventCatCombo.categoryOptions?.get(category.uid)?.displayName()

            catSelectorBinding.catCombo.setText(selectorDisplay)

            this@setCatCombo.addView(catSelectorBinding.root)
        }
    }
}
