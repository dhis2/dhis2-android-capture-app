package org.dhis2.data.filter

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.Observable
import org.dhis2.animations.collapse
import org.dhis2.animations.expand
import org.dhis2.animations.hide
import org.dhis2.animations.show
import org.dhis2.utils.filters.CatOptionComboFilter
import org.dhis2.utils.filters.FilterItem
import org.dhis2.utils.filters.OrgUnitFilter

@BindingAdapter("expand_view")
fun View.setExpanded(expanded: Boolean) {
    if (expanded && visibility == View.GONE) {
        expand { }
    } else if (!expanded && visibility == View.VISIBLE) {
        collapse { }
    }
}

@BindingAdapter("request_layout")
fun View.setRequestLayout(filterItem: FilterItem) {
    if (filterItem is CatOptionComboFilter || filterItem is OrgUnitFilter) {
        filterItem.observeCount()
            .addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    expand(true) { }
                }
            })
    }
}

@BindingAdapter("animated_visibility")
fun View.setAnimatedVisibility(visible: Boolean) {
    if (visible) {
        show()
    } else {
        hide()
    }
}
