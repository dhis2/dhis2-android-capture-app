package org.dhis2.usescases.searchTrackEntity.ui

import android.transition.TransitionManager
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import org.dhis2.R
import org.dhis2.bindings.display
import org.dhis2.bindings.dp
import org.dhis2.databinding.ActivitySearchBinding
import org.dhis2.usescases.searchTrackEntity.SearchAnalytics
import org.dhis2.usescases.searchTrackEntity.SearchList
import org.dhis2.usescases.searchTrackEntity.SearchTEScreenState
import org.dhis2.usescases.searchTrackEntity.ui.BackdropManager.changeBoundsIf
import org.dhis2.utils.isPortrait

class SearchScreenConfigurator(
    val binding: ActivitySearchBinding,
    val filterIsOpenCallback: (isOpen: Boolean) -> Unit,
) {
    fun configure(screenState: SearchTEScreenState) {
        when (screenState) {
            is SearchAnalytics -> configureLandscapeAnalyticsScreen(true)
            is SearchList ->
                if (isPortrait()) {
                    configureListScreen(screenState)
                } else {
                    configureLandscapeAnalyticsScreen(false)
                    configureLandscapeListScreen(screenState)
                }
        }
    }

    private fun configureListScreen(searchConfiguration: SearchList) {
        when {
            searchConfiguration.searchFilters.isOpened -> openFilters()
            searchConfiguration.searchForm.isOpened -> openSearch()
            else -> closeBackdrop()
        }

        binding.clearFilters?.display(searchConfiguration.displayResetFiltersButton())
        syncButtonVisibility(!searchConfiguration.searchForm.isOpened)
        setFiltersVisibility(!searchConfiguration.searchForm.isOpened)
    }

    private fun configureLandscapeListScreen(searchConfiguration: SearchList) {
        if (searchConfiguration.searchFilters.isOpened) {
            openFilters()
        } else {
            openSearch()
        }

        syncButtonVisibility(true)
        setFiltersVisibility(true)
    }

    private fun configureLandscapeAnalyticsScreen(expanded: Boolean) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.backdropLayout)
        constraintSet.setGuidelinePercent(R.id.backdropGuideDiv, if (expanded) 0.0f else 0.26f)
        TransitionManager.beginDelayedTransition(binding.backdropLayout)
        constraintSet.applyTo(binding.backdropLayout)
    }

    private fun syncButtonVisibility(canBeDisplayed: Boolean) {
        binding.syncButton.visibility = if (canBeDisplayed) View.VISIBLE else View.GONE
    }

    private fun setFiltersVisibility(showFilters: Boolean) {
        binding.filterCounter.visibility =
            if (showFilters && (binding.totalFilters ?: 0) > 0) View.VISIBLE else View.GONE
        binding.searchFilterGeneral.visibility = if (showFilters) View.VISIBLE else View.GONE
    }

    private fun openFilters() {
        if (isPortrait()) {
            binding.programSpinner.visibility = View.VISIBLE
            binding.title.visibility = View.GONE
        }
        binding.filterRecyclerLayout.visibility = View.VISIBLE
        binding.searchContainer.visibility = View.GONE
        if (isPortrait()) binding.navigationBar.hide()
        filterIsOpenCallback(true)
        changeBounds(R.id.filterRecyclerLayout, 16.dp)
    }

    fun closeBackdrop() {
        if (isPortrait()) {
            binding.programSpinner.visibility = View.VISIBLE
            binding.title.visibility = View.GONE
        }
        binding.filterRecyclerLayout.visibility = View.GONE
        binding.searchContainer.visibility = View.GONE
        if (isPortrait()) binding.navigationBar.show()
        filterIsOpenCallback(false)
        changeBounds(R.id.backdropGuideTop, 0)
    }

    private fun openSearch() {
        binding.filterRecyclerLayout.visibility = View.GONE
        if (isPortrait()) {
            binding.programSpinner.visibility = View.GONE
            binding.title.visibility = View.VISIBLE
        }
        binding.searchContainer.visibility = View.VISIBLE
        if (isPortrait()) binding.navigationBar.hide()
        filterIsOpenCallback(false)
        changeBounds(R.id.searchContainer, 0)
    }

    private fun changeBounds(endID: Int, margin: Int) {
        changeBoundsIf(
            isPortrait(),
            binding.backdropLayout,
            endID,
            margin,
        )
    }
}
