package org.dhis2.usescases.main.ui.model

import androidx.compose.runtime.Stable
import org.dhis2.commons.filters.FilterItem
import org.dhis2.usescases.main.MainScreenType
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem

@Stable
data class HomeScreenState(
    val userName: String,
    val navigationBarItems: List<NavigationBarItem<NavigationPage>>,
    val homeFilters: List<FilterItem>,
    val activeFilters: Int,
    val versionToUpdate: VersionToUpdateState,
    val filterButtonVisible: Boolean,
    val bottomNavigationBarVisible: Boolean,
    val syncButtonVisible: Boolean,
    val currentScreen: MainScreenType,
)

val defaultHomeScreenState =
    HomeScreenState(
        userName = "",
        navigationBarItems = emptyList(),
        homeFilters = emptyList(),
        activeFilters = 0,
        versionToUpdate = VersionToUpdateState.None,
        filterButtonVisible = false,
        bottomNavigationBarVisible = false,
        syncButtonVisible = false,
        currentScreen = MainScreenType.Loading,
    )

sealed class VersionToUpdateState {
    data object None : VersionToUpdateState()

    data class New(
        val version: String,
    ) : VersionToUpdateState()

    data object Downloading : VersionToUpdateState()
}
