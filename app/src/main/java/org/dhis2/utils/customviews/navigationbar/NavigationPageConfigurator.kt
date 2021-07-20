package org.dhis2.utils.customviews.navigationbar

import androidx.annotation.IdRes

interface NavigationPageConfigurator {
    fun pageVisibility(@IdRes pageId: Int): Boolean {
        return when (NavigationPage.values().firstOrNull { it.id == pageId }) {
            NavigationPage.DETAILS -> displayDetails()
            NavigationPage.EVENTS -> displayEvents()
            NavigationPage.ANALYTICS -> displayAnalytics()
            NavigationPage.RELATIONSHIPS -> displayRelationships()
            NavigationPage.NOTES -> displayNotes()
            NavigationPage.DATA_ENTRY -> displayDataEntry()
            NavigationPage.LIST_VIEW -> displayListView()
            NavigationPage.MAP_VIEW -> displayMapView()
            NavigationPage.TABLE_VIEW -> displayTableView()
            NavigationPage.TASKS -> displayTasks()
            NavigationPage.PROGRAMS -> displayPrograms()
            null -> false
        }
    }

    fun displayDetails(): Boolean = false
    fun displayEvents(): Boolean = false
    fun displayAnalytics(): Boolean = false
    fun displayRelationships(): Boolean = false
    fun displayNotes(): Boolean = false
    fun displayDataEntry(): Boolean = false
    fun displayListView(): Boolean = false
    fun displayMapView(): Boolean = false
    fun displayTableView(): Boolean = false
    fun displayTasks(): Boolean = false
    fun displayPrograms(): Boolean = false
}
