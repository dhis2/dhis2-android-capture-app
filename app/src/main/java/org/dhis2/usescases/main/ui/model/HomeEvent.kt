package org.dhis2.usescases.main.ui.model

import org.dhis2.commons.filters.FilterManager
import org.dhis2.usescases.main.HomeItemData

sealed interface HomeEvent {
    data class SingleProgramNavigation(
        val homeItemData: HomeItemData,
    ) : HomeEvent

    data class GoToLogin(
        val accountsCount: Int,
        val isDeletion: Boolean,
    ) : HomeEvent

    data object ShowDeleteNotification : HomeEvent

    data object CancelAllNotifications : HomeEvent

    data object ShowGranularSync : HomeEvent

    data object BlockSession : HomeEvent

    data object ShowPinDialog : HomeEvent

    data class PeriodFilterRequest(
        val periodRequest: FilterManager.PeriodRequest,
    ) : HomeEvent

    data object OrgUnitFilterRequest : HomeEvent

    data object ToggleSideMenu : HomeEvent

    data object ToggleFilters : HomeEvent
}
