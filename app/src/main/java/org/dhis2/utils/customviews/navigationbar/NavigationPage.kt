package org.dhis2.utils.customviews.navigationbar

import androidx.annotation.IdRes
import org.dhis2.R

enum class NavigationPage(@IdRes val id: Int) {
    DETAILS(R.id.navigation_details),
    EVENTS(R.id.navigation_events),
    ANALYTICS(R.id.navigation_analytics),
    RELATIONSHIPS(R.id.navigation_relationships),
    NOTES(R.id.navigation_notes),
    DATA_ENTRY(R.id.navigation_data_entry),
    LIST_VIEW(R.id.navigation_list_view),
    MAP_VIEW(R.id.navigation_map_view),
    TABLE_VIEW(R.id.navigation_table_view),
    TASKS(R.id.navigation_tasks),
    PROGRAMS(R.id.navigation_programs),
}
