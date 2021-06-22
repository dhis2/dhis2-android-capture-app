package org.dhis2.usescases.teiDashboard.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.lang.IllegalStateException
import org.dhis2.R
import org.dhis2.usescases.notes.NotesFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.VISUALIZATION_TYPE
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.VisualizationType
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment
import org.dhis2.utils.isLandscape

class DashboardPagerAdapter(
    fa: FragmentActivity,
    private val currentProgram: String?,
    private val teiUid: String,
    private val enrollmentUid: String?
) : FragmentStateAdapter(fa) {

    private var indicatorsFragment: IndicatorsFragment? = null
    private var relationshipFragment: RelationshipFragment? = null

    override fun createFragment(position: Int): Fragment {
        return if (isLandscape()) {
            createLandscapeFragment(position)
        } else {
            createPortraitFragment(position)
        }
    }

    private fun createLandscapeFragment(position: Int): Fragment {
        return when (position) {
            ANALYTICS_LANDSCAPE_POSITION -> IndicatorsFragment().apply {
                arguments = Bundle().apply {
                    putString(VISUALIZATION_TYPE, VisualizationType.TRACKER.name)
                }
            }
            RELATIONSHIPS_LANDSCAPE_POSITION -> RelationshipFragment()
            NOTES_LANDSCAPE_POSITION -> NotesFragment.newTrackerInstance(currentProgram!!, teiUid)
            else -> throw IllegalStateException("Fragment not supported")
        }
    }

    private fun createPortraitFragment(position: Int): Fragment {
        return when (position) {
            DETAILS_PORTRAIT_POSITION ->
                TEIDataFragment.newInstance(currentProgram, teiUid, enrollmentUid)
            ANALYTICS_PORTRAIT_POSITION -> {
                if (indicatorsFragment == null) {
                    indicatorsFragment = IndicatorsFragment().apply {
                        arguments = Bundle().apply {
                            putString(VISUALIZATION_TYPE, VisualizationType.TRACKER.name)
                        }
                    }
                }
                indicatorsFragment!!
            }
            RELATIONSHIPS_PORTRAIT_POSITION -> {
                if (relationshipFragment == null) {
                    relationshipFragment = RelationshipFragment()
                }
                relationshipFragment!!
            }
            NOTES_PORTRAIT_POSITION -> NotesFragment.newTrackerInstance(currentProgram!!, teiUid)
            else -> throw IllegalStateException("Fragment not supported")
        }
    }

    override fun getItemCount() =
        if (isLandscape()) getLandscapeItemCount() else getPortraitItemCount()

    private fun getPortraitItemCount(): Int {
        return if (currentProgram != null) PORTRAIT_DASHBOARD_SIZE else 1
    }

    private fun getLandscapeItemCount(): Int {
        return if (currentProgram != null) {
            LANDSCAPE_DASHBOARD_SIZE
        } else {
            NO_FRAGMENT_DUE_TO_NO_PROGRAM_SELECTED
        }
    }

    fun getNavigationPagePosition(navigationId: Int): Int {
        return when (navigationId) {
            R.id.navigation_details ->
                if (isLandscape()) {
                    NO_POSITION
                } else {
                    DETAILS_PORTRAIT_POSITION
                }
            R.id.navigation_events -> NO_POSITION
            R.id.navigation_analytics ->
                if (isLandscape()) {
                    ANALYTICS_LANDSCAPE_POSITION
                } else {
                    ANALYTICS_PORTRAIT_POSITION
                }
            R.id.navigation_relationships ->
                if (isLandscape()) {
                    RELATIONSHIPS_LANDSCAPE_POSITION
                } else {
                    RELATIONSHIPS_PORTRAIT_POSITION
                }
            R.id.navigation_notes ->
                if (isLandscape()) {
                    NOTES_LANDSCAPE_POSITION
                } else {
                    NOTES_PORTRAIT_POSITION
                }
            else -> NO_POSITION
        }
    }

    companion object {
        const val NO_POSITION = -1
        const val DETAILS_PORTRAIT_POSITION = 0
        const val ANALYTICS_LANDSCAPE_POSITION = 0
        const val ANALYTICS_PORTRAIT_POSITION = 1
        const val RELATIONSHIPS_LANDSCAPE_POSITION = 1
        const val RELATIONSHIPS_PORTRAIT_POSITION = 2
        const val NOTES_LANDSCAPE_POSITION = 2
        const val NOTES_PORTRAIT_POSITION = 3
        const val PORTRAIT_DASHBOARD_SIZE = 4
        const val LANDSCAPE_DASHBOARD_SIZE = 3
        const val NO_FRAGMENT_DUE_TO_NO_PROGRAM_SELECTED = 0
    }
}
