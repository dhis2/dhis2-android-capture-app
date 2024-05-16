package org.dhis2.commons.filters.data

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.BindingAdapter
import androidx.databinding.Observable
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.R
import org.dhis2.commons.animations.collapse
import org.dhis2.commons.animations.expand
import org.dhis2.commons.animations.hide
import org.dhis2.commons.animations.show
import org.dhis2.commons.bindings.clipWithAllRoundedCorners
import org.dhis2.commons.bindings.dp
import org.dhis2.commons.filters.CatOptionComboFilter
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.OrgUnitFilter
import org.dhis2.commons.filters.cat_opt_comb.CatOptCombFilterAdapter
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.sorting.SortingStatus
import timber.log.Timber
import java.lang.Exception

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

@BindingAdapter("clipAllCorners")
fun setAllClipCorners(view: View, cornerRadiusInDp: Int) {
    view.clipWithAllRoundedCorners(cornerRadiusInDp.dp)
}

@BindingAdapter("withCatComboFilterAdapter")
fun setWithCatComboFilterAdapter(recyclerView: RecyclerView, setAdapter: Boolean) {
    if (setAdapter) {
        recyclerView.adapter = CatOptCombFilterAdapter()
    }
}

@BindingAdapter(value = ["sortingItem", "filterType"], requireAll = true)
fun setSortingIcon(sortingIcon: ImageView, sortingItem: SortingItem?, filterType: Filters) {
    if (sortingItem != null) {
        if (sortingItem.filterSelectedForSorting !== filterType) {
            sortingIcon.setImageDrawable(
                AppCompatResources.getDrawable(
                    sortingIcon.context,
                    R.drawable.ic_sort_deactivated,
                ),
            )
        } else {
            when (sortingItem.sortingStatus) {
                SortingStatus.ASC -> sortingIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        sortingIcon.context,
                        R.drawable.ic_sort_ascending,
                    ),
                )
                SortingStatus.DESC -> sortingIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        sortingIcon.context,
                        R.drawable.ic_sort_descending,
                    ),
                )
                SortingStatus.NONE -> sortingIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        sortingIcon.context,
                        R.drawable.ic_sort_deactivated,
                    ),
                )
                else -> sortingIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        sortingIcon.context,
                        R.drawable.ic_sort_deactivated,
                    ),
                )
            }
        }
    }
}

@BindingAdapter(value = ["filterArrow", "filterType"])
fun setFilterArrow(view: View, openFilter: Filters?, filterType: Filters) {
    view.animate().scaleY(
        when {
            openFilter !== filterType -> 1f
            else -> -1f
        },
    ).setDuration(200).start()
}

@BindingAdapter("fromResource")
fun setFromResource(imageView: ImageView, @DrawableRes resource: Int) {
    try {
        imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.context, resource))
    } catch (e: Exception) {
        Timber.e(e)
    }
}
