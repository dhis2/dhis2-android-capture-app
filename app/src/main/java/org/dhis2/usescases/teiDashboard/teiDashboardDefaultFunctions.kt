package org.dhis2.usescases.teiDashboard

import android.content.Context
import androidx.fragment.app.Fragment
import org.dhis2.R
import org.dhis2.usescases.notes.NotesFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

private const val OVERVIEW_DEFAULT_POS = 0
private const val INDICATORS_DEFAULT_POS = 1
private const val RELATIONSHIPS_DEFAULT_POS = 2
private const val NOTES_DEFAULT_POS = 3
private const val INDICATORS_LANDSCAPE_DEFAULT_POS = 0
private const val RELATIONSHIPS_LANDSCAPE_DEFAULT_POS = 1
private const val NOTES_LANDSCAPE_DEFAULT_POS = 2

fun getDefaultLandscapeTabTitle(context: Context, position: Int): String {
    return when (position) {
        INDICATORS_LANDSCAPE_DEFAULT_POS -> context.getString(R.string.dashboard_indicators)
        RELATIONSHIPS_LANDSCAPE_DEFAULT_POS -> context.getString(R.string.dashboard_relationships)
        NOTES_LANDSCAPE_DEFAULT_POS -> context.getString(R.string.dashboard_notes)
        else -> {
            throw IllegalArgumentException("Invalid position")
        }
    }
}

fun getDefaultPortraitTabTitle(context: Context, position: Int): String {
    return when (position) {
        OVERVIEW_DEFAULT_POS -> context.getString(R.string.dashboard_overview)
        INDICATORS_DEFAULT_POS -> context.getString(R.string.dashboard_indicators)
        RELATIONSHIPS_DEFAULT_POS -> context.getString(R.string.dashboard_relationships)
        NOTES_DEFAULT_POS -> context.getString(R.string.dashboard_notes)
        else -> {
            throw IllegalArgumentException("Invalid position")
        }
    }
}

fun createDefaultPortraitTabFragment(
    currentProgram: String?, teiUid: String,
    enrollmentUid: String?, position: Int
): Fragment {
    return when (position) {
        OVERVIEW_DEFAULT_POS -> TEIDataFragment.newInstance(currentProgram, teiUid, enrollmentUid)
        INDICATORS_DEFAULT_POS -> IndicatorsFragment()
        RELATIONSHIPS_DEFAULT_POS -> RelationshipFragment()
        NOTES_DEFAULT_POS -> NotesFragment.newTrackerInstance(currentProgram!!, teiUid)
        else -> throw IllegalStateException("Fragment not supported")
    }
}

fun createDefaultLandscapeTabFragment(
    currentProgram: String?, teiUid: String, position: Int
): Fragment {
    return when (position) {
        INDICATORS_LANDSCAPE_DEFAULT_POS -> IndicatorsFragment()
        RELATIONSHIPS_LANDSCAPE_DEFAULT_POS -> RelationshipFragment()
        NOTES_LANDSCAPE_DEFAULT_POS -> NotesFragment.newTrackerInstance(currentProgram!!, teiUid)
        else -> throw IllegalStateException("Fragment not supported")
    }
}
