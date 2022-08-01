package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.databinding.BindingAdapter
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.databinding.CategorySelectorBinding
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo

@BindingAdapter(value = ["setViewModel", "setCatCombo", "enabled"])
fun LinearLayout.setCatCombo(
    viewModel: EventDetailsViewModel,
    eventCatCombo: EventCatCombo,
    enabled: Boolean?
) {
    if (!eventCatCombo.isDefault) {
        this@setCatCombo.removeAllViews()
        eventCatCombo.categories.forEach { category ->
            val catSelectorBinding: CategorySelectorBinding =
                CategorySelectorBinding.inflate(LayoutInflater.from(context))
            catSelectorBinding.catCombLayout.hint = category.name
            catSelectorBinding.catCombo.isEnabled = enabled ?: true
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

@ExperimentalAnimationApi
@BindingAdapter("setReopen")
fun ComposeView.setReopenButton(viewModel: EventDetailsViewModel) {
    setContent {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        MdcTheme {
            val eventDetail by viewModel.eventDetails.collectAsState()
            ReopenButton(eventDetail.canReopen) { viewModel.onReopenClick() }
        }
    }
}
