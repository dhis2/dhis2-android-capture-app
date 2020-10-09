package org.dhis2.usescases.teiDashboard

import android.content.Context
import androidx.fragment.app.Fragment
import org.dhis2.R
import org.dhis2.usescases.notes.NotesFragment
import org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback.FeedbackFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

private const val OVERVIEW_POS = 0
private const val INDICATORS_POS = 1
private const val RELATIONSHIPS_POS = 2
private const val FEEDBACK_POS = 3
private const val NOTES_POS = 4
private const val INDICATORS_LANDSCAPE_POS = 0
private const val RELATIONSHIPS_LANDSCAPE_POS = 1
private const val FEEDBACK_LANDSCAPE_POS = 2
private const val NOTES_LANDSCAPE_POS = 3

const val MOBILE_DASHBOARD_PORTRAIT_SIZE = 5
const val MOBILE_DASHBOARD_LANDSCAPE_SIZE = 4

fun getLandscapeTabTitle(context: Context, position: Int): String {
    return when (position) {
        INDICATORS_LANDSCAPE_POS -> context.getString(R.string.dashboard_indicators)
        RELATIONSHIPS_LANDSCAPE_POS -> context.getString(R.string.dashboard_relationships)
        FEEDBACK_LANDSCAPE_POS -> context.getString(R.string.dashboard_feedback)
        NOTES_LANDSCAPE_POS -> context.getString(R.string.dashboard_notes)
        else -> {
            throw IllegalArgumentException("Invalid position")
        }
    }
}

fun getPortraitTabTitle(context: Context, position: Int): String {
    return when (position) {
        OVERVIEW_POS -> context.getString(R.string.dashboard_overview)
        INDICATORS_POS -> context.getString(R.string.dashboard_indicators)
        RELATIONSHIPS_POS -> context.getString(R.string.dashboard_relationships)
        FEEDBACK_POS -> context.getString(R.string.dashboard_feedback)
        NOTES_POS -> context.getString(R.string.dashboard_notes)
        else -> {
            throw IllegalArgumentException("Invalid position")
        }
    }
}

fun createPortraitTabFragment(
    currentProgram: String?, teiUid: String,
    enrollmentUid: String?, position: Int
): Fragment {
    return when (position) {
        OVERVIEW_POS -> TEIDataFragment.newInstance(currentProgram, teiUid, enrollmentUid)
        INDICATORS_POS -> IndicatorsFragment()
        RELATIONSHIPS_POS -> RelationshipFragment()
        FEEDBACK_POS -> FeedbackFragment()
        NOTES_POS -> NotesFragment.newTrackerInstance(currentProgram!!, teiUid)
        else -> throw IllegalStateException("Fragment not supported")
    }
}

fun createLandscapeTabFragment(
    currentProgram: String?, teiUid: String, position: Int
): Fragment {
    return when (position) {
        INDICATORS_LANDSCAPE_POS -> IndicatorsFragment()
        RELATIONSHIPS_LANDSCAPE_POS -> RelationshipFragment()
        FEEDBACK_LANDSCAPE_POS -> FeedbackFragment()
        NOTES_LANDSCAPE_POS -> NotesFragment.newTrackerInstance(currentProgram!!, teiUid)
        else -> throw IllegalStateException("Fragment not supported")
    }
}
