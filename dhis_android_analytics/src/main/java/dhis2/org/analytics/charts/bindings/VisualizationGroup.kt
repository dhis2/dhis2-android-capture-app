package dhis2.org.analytics.charts.bindings

import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup

fun AnalyticsDhisVisualizationsGroup.hasGroup(groupUid: String?): Boolean {
    return if (groupUid != null) {
        id() == groupUid
    } else {
        true
    }
}
