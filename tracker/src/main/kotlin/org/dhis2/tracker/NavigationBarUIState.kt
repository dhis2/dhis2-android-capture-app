package org.dhis2.tracker

import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem

data class NavigationBarUIState<T>(
    val items: List<NavigationBarItem<T>> = emptyList(),
    val selectedItem: T? = null
)
