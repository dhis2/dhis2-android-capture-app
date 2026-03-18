package org.dhis2.usescases.main.ui.model

import org.dhis2.commons.filters.FilterItem
import org.dhis2.usescases.main.domain.model.BottomNavigationItem

data class HomeScreenState(
    val userName: String,
    val title: String,
    val navigationBarItems: List<BottomNavigationItem>,
    val homeFilters: List<FilterItem>,
    val activeFilters: Int,
    val versionToUpdate: VersionToUpdateState,
    val filterButtonVisible: Boolean,
    val bottomNavigationBarVisible: Boolean,
    val syncButtonVisible: Boolean,
)

val defaultHomeScreenState =
    HomeScreenState(
        userName = "",
        title = "",
        navigationBarItems = emptyList(),
        homeFilters = emptyList(),
        activeFilters = 0,
        versionToUpdate = VersionToUpdateState.None,
        filterButtonVisible = false,
        bottomNavigationBarVisible = false,
        syncButtonVisible = false,
    )

sealed class VersionToUpdateState {
    data object None : VersionToUpdateState()

    data class New(
        val version: String,
    ) : VersionToUpdateState()

    data object Downloading : VersionToUpdateState()
}
